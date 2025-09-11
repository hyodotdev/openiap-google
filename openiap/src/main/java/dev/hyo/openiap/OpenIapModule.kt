package dev.hyo.openiap

import android.app.Activity
import android.content.Context
import java.lang.ref.WeakReference
import android.util.Log
import com.google.gson.Gson
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.android.billingclient.api.*
import dev.hyo.openiap.helpers.ProductManager
import dev.hyo.openiap.listener.OpenIapPurchaseErrorListener
import dev.hyo.openiap.listener.OpenIapPurchaseUpdateListener
import dev.hyo.openiap.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Main OpenIapModule implementation for Android
 * Implements the OpenIapProtocol interface following openiap.dev specification
 */
class OpenIapModule(private val context: Context) : OpenIapProtocol, PurchasesUpdatedListener {
    
    private var billingClient: BillingClient? = null
    // Best-effort promise-style result for debugging, plus event listeners
    private var currentPurchaseCallback: ((Result<List<OpenIapPurchase>>) -> Unit)? = null
    private val purchaseUpdateListeners = mutableSetOf<OpenIapPurchaseUpdateListener>()
    private val purchaseErrorListeners = mutableSetOf<OpenIapPurchaseErrorListener>()
    private var currentActivityRef: WeakReference<Activity>? = null
    private val gson = Gson()
    private val TAG = "OpenIapModule"
    // Product details cache manager
    private val productManager = ProductManager()
    
    init {
        Log.d(TAG, "Initializing BillingClient")
        billingClient = BillingClient
            .newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams
                    .newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .enableAutoServiceReconnection()
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
            initBillingClient(
                onSuccess = { _ ->
                    if (continuation.isActive) continuation.resume(true)
                },
                onFailure = { _ ->
                    if (continuation.isActive) continuation.resume(false)
                }
            )
        }
    }

    
    override suspend fun endConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                billingClient?.endConnection()
                productManager.clear()
                billingClient = null
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
            if (billingClient == null || billingClient?.isReady != true) {
                throw OpenIapError.NotPrepared
            }
            if (params.skus.isEmpty()) {
                throw OpenIapError.EmptySkuList
            }

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
    
    // moved below with other private helpers
    
    // ============================================================================
    // Purchase Methods
    // ============================================================================
    
    override suspend fun requestPurchase(
        request: RequestPurchaseAndroidProps,
        type: ProductRequest.ProductRequestType
    ): List<OpenIapPurchase> = withContext(Dispatchers.IO) {
        val activity = currentActivityRef?.get() ?: (context as? Activity)
        if (activity == null) {
            // Event-based error reporting
            val err = OpenIapError.MissingCurrentActivity
            purchaseErrorListeners.forEach { runCatching { it.onPurchaseError(err) } }
            return@withContext emptyList()
        }

        suspendCancellableCoroutine { continuation ->
            currentPurchaseCallback = { result ->
                continuation.resume(result.getOrElse { emptyList() })
            }
            val desiredType = if (type == ProductRequest.ProductRequestType.SUBS)
                BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP

            val client = billingClient
            if (client == null || client.isReady != true) {
                val err = OpenIapError.NotPrepared
                purchaseErrorListeners.forEach { runCatching { it.onPurchaseError(err) } }
                currentPurchaseCallback?.invoke(Result.success(emptyList()))
                return@suspendCancellableCoroutine
            }

            // Prefer cached details if available
            val cachedDetails = request.skus
                .mapNotNull { sku -> productManager.get(sku) }
                .firstOrNull { it.productType == desiredType }

            val useDetails: (ProductDetails) -> Unit = details@{ productDetails ->
                Log.d(TAG, "Using ProductDetails: sku=${productDetails.productId}, title=${productDetails.title}, type=$type")
                val pdParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                if (type == ProductRequest.ProductRequestType.SUBS) {
                    val offerToken = productDetails.subscriptionOfferDetails
                        ?.firstOrNull()
                        ?.offerToken
                    if (offerToken.isNullOrEmpty()) {
                        Log.w(TAG, "No subscription offer available for ${request.skus}")
                        val err = OpenIapError.SkuOfferMismatch
                        purchaseErrorListeners.forEach { runCatching { it.onPurchaseError(err) } }
                        currentPurchaseCallback?.invoke(Result.success(emptyList()))
                        return@details
                    }
                    Log.d(TAG, "Using offerToken=$offerToken for SUBS purchase")
                    pdParamsBuilder.setOfferToken(offerToken)
                }
                val productDetailsParamsList = listOf(pdParamsBuilder.build())

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .setIsOfferPersonalized(request.isOfferPersonalized == true)
                    .apply {
                        request.obfuscatedAccountIdAndroid?.let { setObfuscatedAccountId(it) }
                        request.obfuscatedProfileIdAndroid?.let { setObfuscatedProfileId(it) }
                    }
                    .build()

                val flowResult = client.launchBillingFlow(activity, billingFlowParams)
                Log.d(TAG, "launchBillingFlow result=${flowResult.responseCode} msg=${flowResult.debugMessage}")
                if (flowResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    val err = OpenIapError.PurchaseFailed(flowResult.debugMessage ?: "Billing flow failed")
                    purchaseErrorListeners.forEach { runCatching { it.onPurchaseError(err) } }
                    currentPurchaseCallback?.invoke(Result.success(emptyList()))
                }
            }

            if (cachedDetails != null) {
                useDetails(cachedDetails)
            } else {
                // Query product details, update cache, then launch flow
                val productList = request.skus.map { sku ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(sku)
                        .setProductType(desiredType)
                        .build()
                }
                val queryParams = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()

                client.queryProductDetailsAsync(queryParams) { billingResult, productDetailsResult ->
                    val productDetailsList = productDetailsResult?.productDetailsList
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                        productDetailsList != null && productDetailsList.isNotEmpty()
                    ) {
                        // Update cache
                        productManager.putAll(productDetailsList)
                        val productDetails = productDetailsList.first()
                        useDetails(productDetails)
                        // Do not complete here; wait for onPurchasesUpdated to resolve the result
                    } else {
                        Log.w(TAG, "queryProductDetails failed: code=${billingResult.responseCode} msg=${billingResult.debugMessage}")
                        val err = OpenIapError.QueryProduct(billingResult.debugMessage ?: "Product not found: ${request.skus}")
                        purchaseErrorListeners.forEach { runCatching { it.onPurchaseError(err) } }
                        currentPurchaseCallback?.invoke(Result.success(emptyList()))
                    }
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

    override suspend fun getActiveSubscriptions(subscriptionIds: List<String>?): List<OpenIapActiveSubscription> =
        withContext(Dispatchers.IO) {
            val subs = queryPurchases(BillingClient.ProductType.SUBS)
            val filtered = if (subscriptionIds.isNullOrEmpty()) subs else subs.filter { it.productId in subscriptionIds }
            filtered.map {
                OpenIapActiveSubscription(
                    productId = it.productId,
                    isActive = true,
                    transactionId = it.id,
                    purchaseToken = it.purchaseToken,
                    transactionDate = it.transactionDate,
                    platform = "android",
                    autoRenewingAndroid = it.autoRenewingAndroid
                )
            }
        }

    override suspend fun hasActiveSubscriptions(subscriptionIds: List<String>?): Boolean =
        withContext(Dispatchers.IO) {
            getActiveSubscriptions(subscriptionIds).isNotEmpty()
        }

    override suspend fun getAvailableItems(type: ProductRequest.ProductRequestType): List<OpenIapPurchase> =
        withContext(Dispatchers.IO) {
            val productType = if (type == ProductRequest.ProductRequestType.SUBS) BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP
            queryPurchases(productType)
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

    override suspend fun validateReceipt(options: ReceiptValidationProps): ReceiptValidationResultAndroid? =
        validateReceipt(options.sku, options.androidOptions)
    
    // ============================================================================
    // PurchasesUpdatedListener Implementation
    // ============================================================================
    
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        Log.d(TAG, "onPurchasesUpdated: code=${billingResult.responseCode} msg=${billingResult.debugMessage} count=${purchases?.size ?: 0}")
        purchases?.forEachIndexed { index, p ->
            Log.d(TAG, "[Purchase $index] token=${p.purchaseToken} orderId=${p.orderId} state=${p.purchaseState} autoRenew=${p.isAutoRenewing} acknowledged=${p.isAcknowledged} products=${p.products} originalJson=${p.originalJson}")
        }
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            val mapped = purchases.map { p ->
                convertToOpenIapPurchase(
                    p,
                    if (p.products.any { it.contains("subs") }) BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP
                )
            }
            Log.d(TAG, "Mapped OpenIapPurchases=${gson.toJson(mapped)}")
            // Broadcast each event to listeners
            mapped.forEach { openIapPurchase ->
                purchaseUpdateListeners.forEach { listener ->
                    runCatching { listener.onPurchaseUpdated(openIapPurchase) }
                }
            }
            currentPurchaseCallback?.invoke(Result.success(mapped))
        } else {
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    Log.i(TAG, "User cancelled purchase flow")
                    val err = dev.hyo.openiap.OpenIapError.UserCancelled
                    purchaseErrorListeners.forEach { listener ->
                        runCatching { listener.onPurchaseError(err) }
                    }
                    currentPurchaseCallback?.invoke(Result.success(emptyList()))
                }
                else -> {
                    val error = dev.hyo.openiap.OpenIapError.fromBillingResponseCode(
                        billingResult.responseCode,
                        billingResult.debugMessage
                    )
                    Log.w(TAG, "Purchase failed: code=${billingResult.responseCode} msg=${error.message}")
                    // Surface framework-specific error upstream (maintains type for UserCancelled, etc.)
                    purchaseErrorListeners.forEach { listener ->
                        runCatching { listener.onPurchaseError(error) }
                    }
                    currentPurchaseCallback?.invoke(Result.success(emptyList()))
                }
            }
        }
        currentPurchaseCallback = null
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

    override suspend fun deepLinkToSubscriptions(options: DeepLinkOptions) {
        val pkg = options.packageNameAndroid ?: context.packageName
        val uri = if (!options.skuAndroid.isNullOrEmpty()) {
            android.net.Uri.parse("https://play.google.com/store/account/subscriptions?sku=${options.skuAndroid}&package=$pkg")
        } else {
            android.net.Uri.parse("https://play.google.com/store/account/subscriptions?package=$pkg")
        }
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override suspend fun getStorefront(): String = withContext(Dispatchers.IO) {
        val client = billingClient ?: return@withContext ""
        suspendCancellableCoroutine { continuation ->
            try {
                client.getBillingConfigAsync(
                    GetBillingConfigParams.newBuilder().build(),
                    BillingConfigResponseListener { result: BillingResult, config: BillingConfig? ->
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            continuation.resume(config?.countryCode.orEmpty())
                        } else {
                            continuation.resume("")
                        }
                    }
                )
            } catch (_: Exception) {
                continuation.resume("")
            }
        }
    }

    override fun addPurchaseUpdateListener(listener: OpenIapPurchaseUpdateListener) {
        purchaseUpdateListeners.add(listener)
    }

    override fun removePurchaseUpdateListener(listener: OpenIapPurchaseUpdateListener) {
        purchaseUpdateListeners.remove(listener)
    }

    override fun addPurchaseErrorListener(listener: OpenIapPurchaseErrorListener) {
        purchaseErrorListeners.add(listener)
    }

    override fun removePurchaseErrorListener(listener: OpenIapPurchaseErrorListener) {
        purchaseErrorListeners.remove(listener)
    }

    // ============================================================================
    // Private Helpers (keep private methods below public API)
    // ============================================================================

    /**
     * Restore purchases (consumables not yet consumed, non-consumables not finished, active subs)
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

    private suspend fun queryProducts(skus: List<String>, productType: String): List<OpenIapProduct> {
        val client = billingClient ?: throw OpenIapError.NotPrepared
        if (client.isReady != true) throw OpenIapError.NotPrepared
        val details = productManager.getOrQuery(client, skus, productType)
        return details.map { convertToOpenIapProduct(it, productType) }
    }

    // Initializes BillingClient and starts the connection. Calls callback on success.
    private fun initBillingClient(
        onSuccess: (billingClient: BillingClient) -> Unit,
        onFailure: (Throwable?) -> Unit = {}
    ) {
        if (GoogleApiAvailability
                .getInstance()
                .isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS
        ) {
            Log.i(TAG, "Google Play Services are not available on this device")
            onFailure(IllegalStateException("Google Play Services are not available on this device"))
            return
        }

        billingClient =
            BillingClient
                .newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(
                    PendingPurchasesParams
                        .newBuilder()
                        .enableOneTimeProducts()
                        .build()
                )
                .enableAutoServiceReconnection() // Automatically handle service disconnections
                .build()

        billingClient?.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                        Log.w(
                            TAG,
                            "Billing setup finished with error: ${billingResult.debugMessage}",
                        )
                        onFailure(IllegalStateException(billingResult.debugMessage ?: "Billing setup failed"))
                        return
                    }
                    onSuccess(billingClient!!)
                }

                override fun onBillingServiceDisconnected() {
                    Log.i(TAG, "Billing service disconnected")
                }
            },
        )
    }

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
}
