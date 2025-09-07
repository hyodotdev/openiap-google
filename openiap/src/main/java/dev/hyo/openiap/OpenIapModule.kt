package dev.hyo.openiap

import android.app.Activity
import android.content.Context
import java.lang.ref.WeakReference
import android.util.Log
import com.google.gson.Gson
import com.android.billingclient.api.*
import dev.hyo.openiap.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Main OpenIapModule implementation for Android
 * Implements the OpenIapProtocol interface following openiap.dev specification
 */
class OpenIapModule(private val context: Context) : OpenIapProtocol, PurchasesUpdatedListener {
    
    private var billingClient: BillingClient? = null
    private var currentPurchaseCallback: ((Result<OpenIapPurchase?>) -> Unit)? = null
    private var currentActivityRef: WeakReference<Activity>? = null
    private val gson = Gson()
    private val TAG = "OpenIapModule"
    
    init {
        Log.d(TAG, "Initializing BillingClient")
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
    }
    
    // Allow UI to provide an Activity for billing flow
    fun setActivity(activity: Activity?) {
        currentActivityRef = activity?.let { WeakReference(it) }
    }
    
    // ============================================================================
    // Connection Methods
    // ============================================================================
    
    override suspend fun initConnection(): Boolean = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        continuation.resume(true)
                    } else {
                        continuation.resume(false)
                    }
                }
                
                override fun onBillingServiceDisconnected() {
                    // Try to restart the connection on the next request
                }
            }) ?: continuation.resume(false)
        }
    }
    
    override suspend fun endConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                billingClient?.endConnection()
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    // ============================================================================
    // Product Methods
    // ============================================================================
    
    override suspend fun fetchProducts(params: ProductRequest): List<OpenIapProduct> = 
        withContext(Dispatchers.IO) {
            val products = mutableListOf<OpenIapProduct>()
            
            // Fetch in-app products if requested
            if (params.type == ProductRequest.ProductRequestType.INAPP || 
                params.type == ProductRequest.ProductRequestType.ALL) {
                val inappProducts = queryProducts(params.skus, BillingClient.ProductType.INAPP)
                products.addAll(inappProducts)
            }
            
            // Fetch subscription products if requested
            if (params.type == ProductRequest.ProductRequestType.SUBS || 
                params.type == ProductRequest.ProductRequestType.ALL) {
                val subsProducts = queryProducts(params.skus, BillingClient.ProductType.SUBS)
                products.addAll(subsProducts)
            }
            
            products
        }
    
    private suspend fun queryProducts(skus: List<String>, productType: String): List<OpenIapProduct> =
        suspendCancellableCoroutine { continuation ->
            val productList = skus.map { sku ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(sku)
                    .setProductType(productType)
                    .build()
            }
            
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()
            
            billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList != null) {
                    val products = productDetailsList.map { productDetails ->
                        convertToOpenIapProduct(productDetails, productType)
                    }
                    continuation.resume(products)
                } else {
                    continuation.resumeWithException(
                        Exception("Failed to fetch products: ${billingResult.debugMessage}")
                    )
                }
            } ?: continuation.resume(emptyList())
        }
    
    // ============================================================================
    // Purchase Methods
    // ============================================================================
    
    override suspend fun requestPurchase(
        request: RequestPurchaseAndroidProps,
        type: ProductRequest.ProductRequestType
    ): OpenIapPurchase? = withContext(Dispatchers.IO) {
        val activity = currentActivityRef?.get() ?: (context as? Activity)
            ?: throw dev.hyo.openiap.helpers.OpenIapError.MissingCurrentActivity
        
        suspendCancellableCoroutine { continuation ->
            currentPurchaseCallback = { result ->
                continuation.resumeWith(result)
            }
            
            // Query product details first
            val productList = request.skus.map { sku ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(sku)
                    .setProductType(
                        if (type == ProductRequest.ProductRequestType.SUBS) 
                            BillingClient.ProductType.SUBS 
                        else BillingClient.ProductType.INAPP
                    )
                    .build()
            }
            
            val queryParams = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()
            
            billingClient?.queryProductDetailsAsync(queryParams) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && 
                    productDetailsList != null && productDetailsList.isNotEmpty()) {
                    
                    val productDetails = productDetailsList.first()
                    Log.d(TAG, "queryProductDetails OK: sku=${productDetails.productId}, title=${productDetails.title}, type=$type")
                    val pdParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                    // For subscriptions, offerToken is required when base plans/offers exist
                    if (type == ProductRequest.ProductRequestType.SUBS) {
                        val offerToken = productDetails.subscriptionOfferDetails
                            ?.firstOrNull()
                            ?.offerToken
                        if (offerToken.isNullOrEmpty()) {
                            Log.w(TAG, "No subscription offer available for ${request.skus}")
                            currentPurchaseCallback = null
                            continuation.resumeWithException(
                                Exception("No subscription offer available for ${request.skus}")
                            )
                            return@queryProductDetailsAsync
                        }
                        Log.d(TAG, "Using offerToken=$offerToken for SUBS purchase")
                        pdParamsBuilder.setOfferToken(offerToken)
                    }
                    val productDetailsParamsList = listOf(pdParamsBuilder.build())
                    
                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .apply {
                            request.obfuscatedAccountIdAndroid?.let { setObfuscatedAccountId(it) }
                            request.obfuscatedProfileIdAndroid?.let { setObfuscatedProfileId(it) }
                        }
                        .build()
                    
                    val flowResult = billingClient?.launchBillingFlow(activity, billingFlowParams)
                    Log.d(TAG, "launchBillingFlow result=${flowResult?.responseCode} msg=${flowResult?.debugMessage}")
                    if (flowResult == null || flowResult.responseCode != BillingClient.BillingResponseCode.OK) {
                        currentPurchaseCallback = null
                        continuation.resumeWithException(
                            Exception("Billing flow failed: ${flowResult?.debugMessage ?: "unknown"}")
                        )
                    }
                } else {
                    Log.w(TAG, "queryProductDetails failed: code=${billingResult.responseCode} msg=${billingResult.debugMessage}")
                    currentPurchaseCallback = null
                    continuation.resumeWithException(
                        Exception("Product not found: ${request.skus}")
                    )
                }
            }
        }
    }
    
    override suspend fun finishTransaction(params: FinishTransactionParams): PurchaseResult =
        withContext(Dispatchers.IO) {
            val purchase = params.purchase
            
            if (params.isConsumable) {
                // Consume the purchase for consumable items
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken ?: purchase.purchaseTokenAndroid ?: "")
                    .build()
                
                suspendCancellableCoroutine { continuation ->
                    billingClient?.consumeAsync(consumeParams) { billingResult, _ ->
                        Log.d(TAG, "consumeAsync: code=${billingResult.responseCode} msg=${billingResult.debugMessage}")
                        continuation.resume(
                            PurchaseResult(
                                responseCode = billingResult.responseCode,
                                debugMessage = billingResult.debugMessage,
                                purchaseToken = purchase.purchaseToken
                            )
                        )
                    } ?: continuation.resume(
                        PurchaseResult(
                            responseCode = -1,
                            message = "Billing client not connected"
                        )
                    )
                }
            } else {
                // Acknowledge the purchase for non-consumable items and subscriptions
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken ?: purchase.purchaseTokenAndroid ?: "")
                    .build()
                
                suspendCancellableCoroutine { continuation ->
                    billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                        Log.d(TAG, "acknowledgePurchase: code=${billingResult.responseCode} msg=${billingResult.debugMessage}")
                        continuation.resume(
                            PurchaseResult(
                                responseCode = billingResult.responseCode,
                                debugMessage = billingResult.debugMessage,
                                purchaseToken = purchase.purchaseToken
                            )
                        )
                    } ?: continuation.resume(
                        PurchaseResult(
                            responseCode = -1,
                            message = "Billing client not connected"
                        )
                    )
                }
            }
        }
    
    // ============================================================================
    // Purchase History Methods
    // ============================================================================
    
    override suspend fun getAvailablePurchases(options: PurchaseOptions?): List<OpenIapPurchase> =
        withContext(Dispatchers.IO) {
            // Internally use restorePurchases to get all purchases
            restorePurchases()
        }
    
    /**
     * Private method to restore purchases
     * Called internally by getAvailablePurchases
     */
    private suspend fun restorePurchases(): List<OpenIapPurchase> {
        val allPurchases = mutableListOf<OpenIapPurchase>()
        
        // Query in-app purchases
        val inappPurchases = queryPurchases(BillingClient.ProductType.INAPP)
        allPurchases.addAll(inappPurchases)
        
        // Query subscription purchases
        val subsPurchases = queryPurchases(BillingClient.ProductType.SUBS)
        allPurchases.addAll(subsPurchases)
        
        return allPurchases
    }
    
    private suspend fun queryPurchases(productType: String): List<OpenIapPurchase> =
        suspendCancellableCoroutine { continuation ->
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(productType)
                .build()
            
            billingClient?.queryPurchasesAsync(params) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val purchases = purchasesList.map { purchase ->
                        convertToOpenIapPurchase(purchase, productType)
                    }
                    continuation.resume(purchases)
                } else {
                    continuation.resume(emptyList())
                }
            } ?: continuation.resume(emptyList())
        }
    
    // ============================================================================
    // Receipt Validation
    // ============================================================================
    
    override suspend fun validateReceipt(
        sku: String,
        androidOptions: ReceiptValidationProps.AndroidValidationOptions?
    ): ReceiptValidationResultAndroid? {
        // Receipt validation should be done server-side
        // This is a placeholder for the validation logic
        // In production, send the purchase token to your backend for validation
        return null
    }
    
    // ============================================================================
    // PurchasesUpdatedListener Implementation
    // ============================================================================
    
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        Log.d(TAG, "onPurchasesUpdated: code=${billingResult.responseCode} msg=${billingResult.debugMessage} count=${purchases?.size ?: 0}")
        purchases?.forEachIndexed { index, p ->
            Log.d(TAG, "[Purchase $index] token=${p.purchaseToken} orderId=${p.orderId} state=${p.purchaseState} autoRenew=${p.isAutoRenewing} acknowledged=${p.isAcknowledged} products=${p.products} originalJson=${p.originalJson}")
        }
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            val purchase = purchases.firstOrNull()
            if (purchase != null) {
                val openIapPurchase = convertToOpenIapPurchase(
                    purchase,
                    if (purchase.products.any { it.contains("subs") }) 
                        BillingClient.ProductType.SUBS 
                    else BillingClient.ProductType.INAPP
                )
                Log.d(TAG, "Mapped OpenIapPurchase=${gson.toJson(openIapPurchase)}")
                currentPurchaseCallback?.invoke(Result.success(openIapPurchase))
            } else {
                Log.d(TAG, "No purchase in list")
                currentPurchaseCallback?.invoke(Result.success(null))
            }
        } else {
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    Log.i(TAG, "User cancelled purchase flow")
                    currentPurchaseCallback?.invoke(
                        Result.failure(dev.hyo.openiap.helpers.OpenIapError.UserCancelled)
                    )
                }
                else -> {
                    val code = OpenIapErrorCode.fromBillingResponseCode(billingResult.responseCode)
                    val message = billingResult.debugMessage
                        ?: when (code) {
                            OpenIapErrorCode.E_ALREADY_OWNED -> "Item already owned"
                            OpenIapErrorCode.E_ITEM_UNAVAILABLE -> "Item unavailable"
                            OpenIapErrorCode.E_DEVELOPER_ERROR -> "Developer error"
                            OpenIapErrorCode.E_SERVICE_UNAVAILABLE -> "Service unavailable"
                            else -> "Purchase failed"
                        }
                    Log.w(TAG, "Purchase failed: code=${code.name} msg=$message")
                    currentPurchaseCallback?.invoke(Result.failure(Exception(message)))
                }
            }
        }
        currentPurchaseCallback = null
    }
    
    // ============================================================================
    // Helper Methods
    // ============================================================================
    
    private fun convertToOpenIapProduct(
        productDetails: ProductDetails,
        productType: String
    ): OpenIapProduct {
        val oneTimeOffer = productDetails.oneTimePurchaseOfferDetails
        val subsOffer = productDetails.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()
        
        val displayPrice = if (productType == BillingClient.ProductType.SUBS) {
            subsOffer?.formattedPrice ?: ""
        } else {
            oneTimeOffer?.formattedPrice ?: ""
        }
        
        val currency = if (productType == BillingClient.ProductType.SUBS) {
            subsOffer?.priceCurrencyCode ?: ""
        } else {
            oneTimeOffer?.priceCurrencyCode ?: ""
        }
        
        val priceAmount = if (productType == BillingClient.ProductType.SUBS) {
            subsOffer?.priceAmountMicros ?: 0L
        } else {
            oneTimeOffer?.priceAmountMicros ?: 0L
        }
        
        return OpenIapProduct(
            id = productDetails.productId,
            title = productDetails.title,
            description = productDetails.description,
            type = if (productType == BillingClient.ProductType.SUBS) 
                OpenIapProduct.ProductType.SUBS 
            else OpenIapProduct.ProductType.INAPP,
            displayName = productDetails.name,
            displayPrice = displayPrice,
            currency = currency,
            price = priceAmount.toDouble() / 1_000_000.0,
            platform = "android",
            nameAndroid = productDetails.name,
            oneTimePurchaseOfferDetailsAndroid = productDetails.oneTimePurchaseOfferDetails?.let {
                OneTimePurchaseOfferDetail(
                    priceCurrencyCode = it.priceCurrencyCode,
                    formattedPrice = it.formattedPrice,
                    priceAmountMicros = it.priceAmountMicros.toString()
                )
            },
            subscriptionOfferDetailsAndroid = productDetails.subscriptionOfferDetails?.map { offer ->
                SubscriptionOfferDetail(
                    basePlanId = offer.basePlanId,
                    offerId = offer.offerId,
                    offerToken = offer.offerToken,
                    offerTags = offer.offerTags,
                    pricingPhases = PricingPhases(
                        pricingPhaseList = offer.pricingPhases.pricingPhaseList.map { phase ->
                            PricingPhase(
                                formattedPrice = phase.formattedPrice,
                                priceCurrencyCode = phase.priceCurrencyCode,
                                billingPeriod = phase.billingPeriod,
                                billingCycleCount = phase.billingCycleCount,
                                priceAmountMicros = phase.priceAmountMicros.toString(),
                                recurrenceMode = phase.recurrenceMode
                            )
                        }
                    )
                )
            }
        )
    }
    
    private fun convertToOpenIapPurchase(
        purchase: Purchase,
        productType: String
    ): OpenIapPurchase {
        return OpenIapPurchase(
            id = purchase.purchaseToken,
            productId = purchase.products.firstOrNull() ?: "",
            ids = purchase.products,
            transactionId = purchase.orderId,
            transactionDate = purchase.purchaseTime,
            transactionReceipt = purchase.originalJson,
            purchaseToken = purchase.purchaseToken,
            platform = "android",
            quantity = purchase.quantity,
            purchaseState = OpenIapPurchase.PurchaseState.fromBillingClientState(purchase.purchaseState),
            isAutoRenewing = purchase.isAutoRenewing,
            purchaseTokenAndroid = purchase.purchaseToken,
            dataAndroid = purchase.originalJson,
            signatureAndroid = purchase.signature,
            autoRenewingAndroid = purchase.isAutoRenewing,
            isAcknowledgedAndroid = purchase.isAcknowledged,
            packageNameAndroid = purchase.packageName,
            developerPayloadAndroid = purchase.developerPayload,
            obfuscatedAccountIdAndroid = purchase.accountIdentifiers?.obfuscatedAccountId,
            obfuscatedProfileIdAndroid = purchase.accountIdentifiers?.obfuscatedProfileId
        )
    }

    // ============================================================================
    // Android-Specific APIs
    // ============================================================================
    
    override suspend fun acknowledgePurchaseAndroid(purchaseToken: String) {
        withContext(Dispatchers.IO) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()
            
            billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    println("Failed to acknowledge purchase: ${billingResult.debugMessage}")
                }
            }
        }
    }
    
    override suspend fun consumePurchaseAndroid(purchaseToken: String) {
        withContext(Dispatchers.IO) {
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()
            
            billingClient?.consumeAsync(consumeParams) { billingResult, _ ->
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    println("Failed to consume purchase: ${billingResult.debugMessage}")
                }
            }
        }
    }
    
    override suspend fun flushFailedPurchaseCachedAsPendingAndroid() {
        // Google Play Billing doesn't have a direct API for this
        // Typically handled by clearing internal caches or retrying pending purchases
        println("flushFailedPurchaseCachedAsPendingAndroid called - no direct Google Play Billing API available")
    }
}
