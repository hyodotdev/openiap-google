package dev.hyo.openiap.horizon

import android.app.Activity
import android.content.Context
import com.meta.horizon.billingclient.api.*
import dev.hyo.openiap.OpenIapError
import dev.hyo.openiap.OpenIapLog
import dev.hyo.openiap.OpenIapProtocol
import dev.hyo.openiap.listener.OpenIapPurchaseErrorListener
import dev.hyo.openiap.listener.OpenIapPurchaseUpdateListener
import dev.hyo.openiap.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class OpenIapHorizonModule(
    private val context: Context,
    private val appId: String? = null,
) : OpenIapProtocol, PurchasesUpdatedListener {

    private val TAG = "OpenIapHorizon"
    private var billingClient: BillingClient? = null
    private var currentActivity: Activity? = null
    private var currentPurchaseCallback: ((Result<List<OpenIapPurchase>>) -> Unit)? = null

    private val purchaseUpdateListeners = mutableSetOf<OpenIapPurchaseUpdateListener>()
    private val purchaseErrorListeners = mutableSetOf<OpenIapPurchaseErrorListener>()

    fun setActivity(activity: Activity?) { currentActivity = activity }

    override suspend fun initConnection(): Boolean = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { cont ->
            try {
                val builder = BillingClient.newBuilder(context)
                    .setListener(this@OpenIapHorizonModule)
                    .enablePendingPurchases(
                        PendingPurchasesParams
                            .newBuilder()
                            .enableOneTimeProducts()
                            .build()
                    )
                if (!appId.isNullOrEmpty()) builder.setAppId(appId)
                billingClient = builder.build()
                billingClient?.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            OpenIapLog.i("Horizon billing connected", TAG)
                            cont.resume(true)
                        } else {
                            OpenIapLog.w("Horizon setup failed: ${billingResult.debugMessage}", TAG)
                            cont.resume(false)
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        OpenIapLog.i("Horizon service disconnected", TAG)
                    }
                })
            } catch (t: Throwable) {
                OpenIapLog.w("Horizon init error: ${t.message}", TAG)
                cont.resume(false)
            }
        }
    }

    override suspend fun endConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            billingClient?.endConnection()
            billingClient = null
            true
        } catch (t: Throwable) { false }
    }

    override suspend fun fetchProducts(params: ProductRequest): List<OpenIapProduct> = withContext(Dispatchers.IO) {
        val client = billingClient ?: throw OpenIapError.NotPrepared
        if (params.skus.isEmpty()) throw OpenIapError.EmptySkuList

        val results = mutableListOf<OpenIapProduct>()

        suspend fun query(type: String) {
            val productList = params.skus.map { sku ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(sku)
                    .setProductType(type)
                    .build()
            }
            val q = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
            val details: List<ProductDetails> = suspendCancellableCoroutine { cont ->
                client.queryProductDetailsAsync(q) { br, pd ->
                    if (br.responseCode != BillingClient.BillingResponseCode.OK) {
                        cont.resume(emptyList())
                        return@queryProductDetailsAsync
                    }
                    val list = pd ?: emptyList()
                    cont.resume(list)
                }
            }
            details.forEach { results.add(convertProduct(it, type)) }
        }

        when (params.type) {
            ProductRequest.ProductRequestType.ALL -> { query(BillingClient.ProductType.INAPP); query(BillingClient.ProductType.SUBS) }
            ProductRequest.ProductRequestType.INAPP -> query(BillingClient.ProductType.INAPP)
            ProductRequest.ProductRequestType.SUBS -> query(BillingClient.ProductType.SUBS)
        }
        results
    }

    override suspend fun getAvailablePurchases(options: PurchaseOptions?): List<OpenIapPurchase> = withContext(Dispatchers.IO) {
        val client = billingClient ?: throw OpenIapError.NotPrepared
        val list = mutableListOf<OpenIapPurchase>()
        suspend fun q(type: String) {
            suspendCancellableCoroutine { cont ->
                client.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder().setProductType(type).build()
                ) { br, purchases ->
                    if (br.responseCode != BillingClient.BillingResponseCode.OK) {
                        cont.resume(emptyList())
                        return@queryPurchasesAsync
                    }
                    cont.resume(purchases ?: emptyList())
                }
            }.forEach { list.add(convertPurchase(it, type)) }
        }
        q(BillingClient.ProductType.INAPP)
        q(BillingClient.ProductType.SUBS)
        list
    }

    override suspend fun getActiveSubscriptions(subscriptionIds: List<String>?): List<OpenIapActiveSubscription> {
        return getAvailablePurchases(null)
            .filter { it.isAutoRenewing == true }
            .filter { subscriptionIds.isNullOrEmpty() || subscriptionIds.contains(it.productId) }
            .map {
                OpenIapActiveSubscription(
                    productId = it.productId,
                    isActive = true,
                    transactionId = it.id,
                    purchaseToken = it.purchaseToken,
                    transactionDate = it.transactionDate,
                    platform = "android",
                    autoRenewingAndroid = it.isAutoRenewing
                )
            }
    }

    override suspend fun hasActiveSubscriptions(subscriptionIds: List<String>?): Boolean =
        getActiveSubscriptions(subscriptionIds).isNotEmpty()

    override suspend fun getAvailableItems(type: ProductRequest.ProductRequestType): List<OpenIapPurchase> =
        getAvailablePurchases(null).filter { it.productId.isNotEmpty() }

    override suspend fun requestPurchase(
        request: RequestPurchaseParams,
        type: ProductRequest.ProductRequestType,
    ): List<OpenIapPurchase> = withContext(Dispatchers.IO) {
        val client = billingClient ?: throw OpenIapError.NotPrepared
        val activity = currentActivity ?: throw OpenIapError.MissingCurrentActivity
        val desiredType = if (type == ProductRequest.ProductRequestType.SUBS) BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP
        if (request.skus.isEmpty()) throw OpenIapError.EmptySkuList

        val products = request.skus.map { sku ->
            QueryProductDetailsParams.Product.newBuilder().setProductId(sku).setProductType(desiredType).build()
        }
        val q = QueryProductDetailsParams.newBuilder().setProductList(products).build()

        suspendCancellableCoroutine<List<OpenIapPurchase>> { cont ->
            currentPurchaseCallback = { result ->
                if (cont.isActive) cont.resume(result.getOrElse { emptyList() })
            }
            client.queryProductDetailsAsync(q) { br, pd ->
                if (br.responseCode != BillingClient.BillingResponseCode.OK) {
                    purchaseErrorListeners.forEach { runCatching { it.onPurchaseError(OpenIapError.QueryProduct()) } }
                    if (cont.isActive) cont.resume(emptyList())
                    return@queryProductDetailsAsync
                }
                val details = pd ?: emptyList()
                if (details.isEmpty()) {
                    purchaseErrorListeners.forEach { runCatching { it.onPurchaseError(OpenIapError.SkuNotFound(request.skus.firstOrNull() ?: "")) } }
                    if (cont.isActive) cont.resume(emptyList())
                    return@queryProductDetailsAsync
                }
                val paramsList = details.map { d ->
                    val b = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(d)
                    if (desiredType == BillingClient.ProductType.SUBS) {
                        d.subscriptionOfferDetails?.firstOrNull()?.offerToken?.let { token -> b.setOfferToken(token) }
                    }
                    b.build()
                }
                val flow = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(paramsList)
                    .setIsOfferPersonalized(request.isOfferPersonalized == true)
                    .apply {
                        request.obfuscatedAccountIdAndroid?.let { id -> setObfuscatedAccountId(id) }
                        request.obfuscatedProfileIdAndroid?.let { id -> setObfuscatedProfileId(id) }
                    }
                    .build()
                val r = client.launchBillingFlow(activity, flow)
                if (r.responseCode != BillingClient.BillingResponseCode.OK) {
                    purchaseErrorListeners.forEach { runCatching { it.onPurchaseError(OpenIapError.PurchaseFailed()) } }
                    if (cont.isActive) cont.resume(emptyList())
                }
                // Wait for onPurchasesUpdated to deliver
            }
        }
    }

    override suspend fun finishTransaction(params: FinishTransactionParams): PurchaseResult = withContext(Dispatchers.IO) {
        val client = billingClient ?: throw OpenIapError.NotPrepared
        val token = params.purchase.purchaseToken ?: params.purchase.purchaseTokenAndroid ?: ""
        if (params.isConsumable) {
            suspendCancellableCoroutine { cont ->
                client.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(token).build()) { br, _ ->
                    cont.resume(PurchaseResult(responseCode = br.responseCode, debugMessage = br.debugMessage, purchaseToken = token))
                }
            }
        } else {
            suspendCancellableCoroutine { cont ->
                client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(token).build()) { br ->
                    cont.resume(PurchaseResult(responseCode = br.responseCode, debugMessage = br.debugMessage, purchaseToken = token))
                }
            }
        }
    }

    override suspend fun validateReceipt(options: ReceiptValidationProps): ReceiptValidationResultAndroid? = null

    override suspend fun acknowledgePurchaseAndroid(purchaseToken: String) {}
    override suspend fun consumePurchaseAndroid(purchaseToken: String) {}

    override suspend fun deepLinkToSubscriptions(options: DeepLinkOptions) {}

    override suspend fun getStorefront(): String {
        val client = billingClient ?: return ""
        return suspendCancellableCoroutine { cont ->
            client.getBillingConfigAsync(GetBillingConfigParams.newBuilder().build(), object : BillingConfigResponseListener {
                override fun onBillingConfigResponse(result: BillingResult, config: BillingConfig?) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        cont.resume(config?.countryCode ?: "")
                    } else {
                        cont.resume("")
                    }
                }
            })
        }
    }

    override fun addPurchaseUpdateListener(listener: OpenIapPurchaseUpdateListener) { purchaseUpdateListeners.add(listener) }
    override fun removePurchaseUpdateListener(listener: OpenIapPurchaseUpdateListener) { purchaseUpdateListeners.remove(listener) }
    override fun addPurchaseErrorListener(listener: OpenIapPurchaseErrorListener) { purchaseErrorListeners.add(listener) }
    override fun removePurchaseErrorListener(listener: OpenIapPurchaseErrorListener) { purchaseErrorListeners.remove(listener) }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        OpenIapLog.d("onPurchasesUpdated code=${billingResult.responseCode} count=${purchases?.size ?: 0}", TAG)
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            val mapped = purchases.map { convertPurchase(it, BillingClient.ProductType.INAPP) }
            mapped.forEach { p -> purchaseUpdateListeners.forEach { runCatching { it.onPurchaseUpdated(p) } } }
            currentPurchaseCallback?.invoke(Result.success(mapped))
            currentPurchaseCallback = null
        } else {
            val err = when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.USER_CANCELED -> OpenIapError.UserCancelled
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> OpenIapError.ItemAlreadyOwned
                BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> OpenIapError.ItemNotOwned
                else -> OpenIapError.PurchaseFailed()
            }
            purchaseErrorListeners.forEach { runCatching { it.onPurchaseError(err) } }
            currentPurchaseCallback?.invoke(Result.success(emptyList()))
            currentPurchaseCallback = null
        }
    }

    private fun convertProduct(details: ProductDetails, productType: String): OpenIapProduct {
        val oneTime = details.oneTimePurchaseOfferDetails
        val subsPhase = details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()
        val displayPrice = if (productType == BillingClient.ProductType.SUBS) {
            subsPhase?.formattedPrice ?: ""
        } else {
            oneTime?.formattedPrice ?: ""
        }
        val currency = if (productType == BillingClient.ProductType.SUBS) {
            subsPhase?.priceCurrencyCode ?: ""
        } else {
            oneTime?.priceCurrencyCode ?: ""
        }
        val priceMicros = if (productType == BillingClient.ProductType.SUBS) {
            subsPhase?.priceAmountMicros ?: 0L
        } else {
            oneTime?.priceAmountMicros ?: 0L
        }
        return OpenIapProduct(
            id = details.productId,
            title = details.title,
            description = details.description,
            type = if (productType == BillingClient.ProductType.SUBS) OpenIapProduct.ProductType.SUBS else OpenIapProduct.ProductType.INAPP,
            displayName = details.name,
            displayPrice = displayPrice,
            currency = currency,
            price = priceMicros.toDouble() / 1_000_000.0,
            platform = "android",
            nameAndroid = details.name,
            oneTimePurchaseOfferDetailsAndroid = oneTime?.let {
                OneTimePurchaseOfferDetail(
                    priceCurrencyCode = it.priceCurrencyCode,
                    formattedPrice = it.formattedPrice,
                    priceAmountMicros = it.priceAmountMicros.toString(),
                )
            },
            subscriptionOfferDetailsAndroid = details.subscriptionOfferDetails?.map { offer ->
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
                                recurrenceMode = phase.recurrenceMode,
                            )
                        }
                    )
                )
            }
        )
    }

    private fun convertPurchase(purchase: Purchase, productType: String): OpenIapPurchase {
        return OpenIapPurchase(
            id = purchase.purchaseToken,
            productId = purchase.products.firstOrNull() ?: "",
            ids = purchase.products,
            transactionId = purchase.orderId,
            transactionDate = purchase.purchaseTime,
            transactionReceipt = purchase.originalJson ?: "",
            purchaseToken = purchase.purchaseToken,
            platform = "android",
            quantity = purchase.quantity,
            purchaseState = OpenIapPurchase.PurchaseState.PURCHASED,
            isAutoRenewing = purchase.isAutoRenewing(),
            purchaseTokenAndroid = purchase.purchaseToken,
            dataAndroid = purchase.originalJson,
            signatureAndroid = purchase.signature,
            autoRenewingAndroid = purchase.isAutoRenewing(),
            isAcknowledgedAndroid = purchase.isAcknowledged(),
            packageNameAndroid = purchase.packageName,
            developerPayloadAndroid = purchase.developerPayload,
            obfuscatedAccountIdAndroid = null,
            obfuscatedProfileIdAndroid = null,
        )
    }
}
