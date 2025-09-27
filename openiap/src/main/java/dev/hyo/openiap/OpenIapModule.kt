package dev.hyo.openiap

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingConfig
import com.android.billingclient.api.BillingConfigResponseListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.GetBillingConfigParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase as BillingPurchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.gson.Gson
import dev.hyo.openiap.helpers.ProductManager
import dev.hyo.openiap.MutationAcknowledgePurchaseAndroidHandler
import dev.hyo.openiap.MutationConsumePurchaseAndroidHandler
import dev.hyo.openiap.MutationDeepLinkToSubscriptionsHandler
import dev.hyo.openiap.MutationEndConnectionHandler
import dev.hyo.openiap.MutationFinishTransactionHandler
import dev.hyo.openiap.MutationInitConnectionHandler
import dev.hyo.openiap.MutationRequestPurchaseHandler
import dev.hyo.openiap.MutationRestorePurchasesHandler
import dev.hyo.openiap.MutationValidateReceiptHandler
import dev.hyo.openiap.MutationHandlers
import dev.hyo.openiap.QueryHandlers
import dev.hyo.openiap.SubscriptionHandlers
import dev.hyo.openiap.QueryFetchProductsHandler
import dev.hyo.openiap.QueryGetActiveSubscriptionsHandler
import dev.hyo.openiap.QueryGetAvailablePurchasesHandler
import dev.hyo.openiap.QueryHasActiveSubscriptionsHandler
import dev.hyo.openiap.RequestPurchaseResultPurchases
import dev.hyo.openiap.SubscriptionPurchaseErrorHandler
import dev.hyo.openiap.SubscriptionPurchaseUpdatedHandler
import dev.hyo.openiap.ReceiptValidationProps
import dev.hyo.openiap.helpers.AndroidPurchaseArgs
import dev.hyo.openiap.helpers.onPurchaseError
import dev.hyo.openiap.helpers.onPurchaseUpdated
import dev.hyo.openiap.helpers.queryProductDetails
import dev.hyo.openiap.helpers.queryPurchases
import dev.hyo.openiap.helpers.restorePurchases as restorePurchasesHelper
import dev.hyo.openiap.helpers.toAndroidPurchaseArgs
import dev.hyo.openiap.listener.OpenIapPurchaseErrorListener
import dev.hyo.openiap.listener.OpenIapPurchaseUpdateListener
import dev.hyo.openiap.utils.BillingConverters.toInAppProduct
import dev.hyo.openiap.utils.BillingConverters.toPurchase
import dev.hyo.openiap.utils.BillingConverters.toSubscriptionProduct
import dev.hyo.openiap.utils.fromBillingState
import dev.hyo.openiap.utils.toActiveSubscription
import dev.hyo.openiap.utils.toProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import java.lang.ref.WeakReference

/**
 * Main OpenIapModule implementation for Android
 */
class OpenIapModule(private val context: Context) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "OpenIapModule"
    }

    private var billingClient: BillingClient? = null
    private var currentActivityRef: WeakReference<Activity>? = null
    private val productManager = ProductManager()
    private val gson = Gson()
    private val fallbackActivity: Activity? = if (context is Activity) context else null

    private val purchaseUpdateListeners = mutableSetOf<OpenIapPurchaseUpdateListener>()
    private val purchaseErrorListeners = mutableSetOf<OpenIapPurchaseErrorListener>()
    private var currentPurchaseCallback: ((Result<List<Purchase>>) -> Unit)? = null

    val initConnection: MutationInitConnectionHandler = {
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine<Boolean> { continuation ->
                initBillingClient(
                    onSuccess = { continuation.resume(true) },
                    onFailure = { err ->
                        OpenIapLog.w("Billing set up failed: ${err?.message}", TAG)
                        continuation.resume(false)
                    }
                )
            }
        }
    }

    val endConnection: MutationEndConnectionHandler = {
        withContext(Dispatchers.IO) {
            runCatching {
                billingClient?.endConnection()
                productManager.clear()
                billingClient = null
            }.fold(onSuccess = { true }, onFailure = { false })
        }
    }

    val fetchProducts: QueryFetchProductsHandler = { params ->
        withContext(Dispatchers.IO) {
            val client = billingClient ?: throw OpenIapError.NotPrepared
            if (!client.isReady) throw OpenIapError.NotPrepared
            if (params.skus.isEmpty() && params.type != ProductQueryType.All) throw OpenIapError.EmptySkuList

            val queryType = params.type ?: ProductQueryType.All
            val includeInApp = queryType == ProductQueryType.InApp || queryType == ProductQueryType.All
            val includeSubs = queryType == ProductQueryType.Subs || queryType == ProductQueryType.All

            val inAppProducts = if (includeInApp) {
                queryProductDetails(client, productManager, params.skus, BillingClient.ProductType.INAPP)
                    .map { it.toInAppProduct() }
            } else emptyList()

            val subscriptionProducts = if (includeSubs) {
                queryProductDetails(client, productManager, params.skus, BillingClient.ProductType.SUBS)
                    .map { it.toSubscriptionProduct() }
            } else emptyList()

            when (queryType) {
                ProductQueryType.InApp -> FetchProductsResultProducts(inAppProducts)
                ProductQueryType.Subs -> FetchProductsResultSubscriptions(subscriptionProducts)
                ProductQueryType.All -> {
                    // For All type, combine products and return as Products result
                    val allProducts = inAppProducts + subscriptionProducts.filterIsInstance<ProductSubscriptionAndroid>().map { it.toProduct() }
                    FetchProductsResultProducts(allProducts)
                }
            }
        }
    }
    val getAvailablePurchases: QueryGetAvailablePurchasesHandler = { _ ->
        withContext(Dispatchers.IO) { restorePurchasesHelper(billingClient) }
    }

    val getActiveSubscriptions: QueryGetActiveSubscriptionsHandler = { subscriptionIds ->
        withContext(Dispatchers.IO) {
            val androidPurchases = queryPurchases(billingClient, BillingClient.ProductType.SUBS)
                .filterIsInstance<PurchaseAndroid>()
            val ids = subscriptionIds.orEmpty()
            val filtered = if (ids.isEmpty()) {
                androidPurchases
            } else {
                androidPurchases.filter { it.productId in ids }
            }
            filtered.map { it.toActiveSubscription() }
        }
    }

    val hasActiveSubscriptions: QueryHasActiveSubscriptionsHandler = { subscriptionIds ->
        getActiveSubscriptions(subscriptionIds).isNotEmpty()
    }

    val requestPurchase: MutationRequestPurchaseHandler = { props ->
        val purchases = withContext(Dispatchers.IO) {
            val androidArgs = props.toAndroidPurchaseArgs()
            val activity = currentActivityRef?.get() ?: fallbackActivity

            if (activity == null) {
                val err = OpenIapError.MissingCurrentActivity
                purchaseErrorListeners.forEach { listener -> runCatching { listener.onPurchaseError(err) } }
                return@withContext emptyList()
            }

            val client = billingClient
            if (client == null || !client.isReady) {
                val err = OpenIapError.NotPrepared
                purchaseErrorListeners.forEach { listener -> runCatching { listener.onPurchaseError(err) } }
                return@withContext emptyList()
            }

            if (androidArgs.skus.isEmpty()) {
                val err = OpenIapError.EmptySkuList
                purchaseErrorListeners.forEach { listener -> runCatching { listener.onPurchaseError(err) } }
                return@withContext emptyList()
            }

            suspendCancellableCoroutine<List<Purchase>> { continuation ->
                currentPurchaseCallback = { result ->
                    if (continuation.isActive) continuation.resume(result.getOrDefault(emptyList()))
                }

                val desiredType = if (androidArgs.type == ProductQueryType.Subs) BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP

                val detailsBySku = mutableMapOf<String, ProductDetails>()
                androidArgs.skus.forEach { sku ->
                    productManager.get(sku)?.takeIf { it.productType == desiredType }?.let { detailsBySku[sku] = it }
                }

                val missing = androidArgs.skus.filter { !detailsBySku.containsKey(it) }

                fun buildAndLaunch(details: List<ProductDetails>) {
                    val paramsList = mutableListOf<BillingFlowParams.ProductDetailsParams>()
                    val requestedOffersBySku = mutableMapOf<String, MutableList<String>>()

                    if (androidArgs.type == ProductQueryType.Subs) {
                        androidArgs.subscriptionOffers.orEmpty().forEach { offer ->
                            if (offer.offerToken.isNotEmpty()) {
                                val queue = requestedOffersBySku.getOrPut(offer.sku) { mutableListOf() }
                                queue.add(offer.offerToken)
                            }
                        }
                    }

                    details.forEachIndexed { index, productDetails ->
                        val builder = BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)

                        if (androidArgs.type == ProductQueryType.Subs) {
                            val availableTokens = productDetails.subscriptionOfferDetails?.map { it.offerToken } ?: emptyList()
                            val fromQueue = requestedOffersBySku[productDetails.productId]?.let { queue ->
                                if (queue.isNotEmpty()) queue.removeAt(0) else null
                            }
                            val fromIndex = androidArgs.subscriptionOffers?.getOrNull(index)?.takeIf { it.sku == productDetails.productId }?.offerToken
                            val resolved = fromQueue ?: fromIndex ?: productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken

                            if (resolved.isNullOrEmpty() || (availableTokens.isNotEmpty() && !availableTokens.contains(resolved))) {
                                val err = OpenIapError.SkuOfferMismatch
                                purchaseErrorListeners.forEach { listener -> runCatching { listener.onPurchaseError(err) } }
                                currentPurchaseCallback?.invoke(Result.success(emptyList()))
                                return
                            }

                            builder.setOfferToken(resolved)
                        }

                        paramsList += builder.build()
                    }

                    val flowBuilder = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(paramsList)
                        .setIsOfferPersonalized(androidArgs.isOfferPersonalized == true)

                    androidArgs.obfuscatedAccountId?.let { flowBuilder.setObfuscatedAccountId(it) }
                    androidArgs.obfuscatedProfileId?.let { flowBuilder.setObfuscatedProfileId(it) }

                    if (androidArgs.type == ProductQueryType.Subs && !androidArgs.purchaseTokenAndroid.isNullOrBlank()) {
                        val updateParamsBuilder = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                            .setOldPurchaseToken(androidArgs.purchaseTokenAndroid)
                        androidArgs.replacementModeAndroid?.let { updateParamsBuilder.setSubscriptionReplacementMode(it) }
                        flowBuilder.setSubscriptionUpdateParams(updateParamsBuilder.build())
                    }

                    val result = client.launchBillingFlow(activity, flowBuilder.build())
                    if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                        val err = OpenIapError.PurchaseFailed
                        purchaseErrorListeners.forEach { listener -> runCatching { listener.onPurchaseError(err) } }
                        currentPurchaseCallback?.invoke(Result.success(emptyList()))
                    }
                }

                if (missing.isEmpty()) {
                    val ordered = androidArgs.skus.mapNotNull { detailsBySku[it] }
                    if (ordered.size != androidArgs.skus.size) {
                        val missingSku = androidArgs.skus.firstOrNull { !detailsBySku.containsKey(it) }
                        val err = OpenIapError.SkuNotFound(missingSku ?: "")
                        purchaseErrorListeners.forEach { listener -> runCatching { listener.onPurchaseError(err) } }
                        currentPurchaseCallback?.invoke(Result.success(emptyList()))
                        return@suspendCancellableCoroutine
                    }
                    buildAndLaunch(ordered)
                } else {
                    val productList = missing.map { sku ->
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(sku)
                            .setProductType(desiredType)
                            .build()
                    }

                    val queryParams = QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                        .build()

                    client.queryProductDetailsAsync(queryParams) { billingResult: BillingResult, result: QueryProductDetailsResult ->
                        val productDetailsList = result.productDetailsList
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !productDetailsList.isNullOrEmpty()) {
                            productManager.putAll(productDetailsList)
                            productDetailsList.forEach { detailsBySku[it.productId] = it }
                            val ordered = androidArgs.skus.mapNotNull { detailsBySku[it] }
                            if (ordered.size != androidArgs.skus.size) {
                                val missingSku = androidArgs.skus.firstOrNull { !detailsBySku.containsKey(it) }
                                val err = OpenIapError.SkuNotFound(missingSku ?: "")
                                purchaseErrorListeners.forEach { listener -> runCatching { listener.onPurchaseError(err) } }
                                currentPurchaseCallback?.invoke(Result.success(emptyList()))
                                return@queryProductDetailsAsync
                            }
                            buildAndLaunch(ordered)
                        } else {
                            val err = OpenIapError.QueryProduct
                            purchaseErrorListeners.forEach { listener -> runCatching { listener.onPurchaseError(err) } }
                            currentPurchaseCallback?.invoke(Result.success(emptyList()))
                        }
                    }
                }
            }
        }
        RequestPurchaseResultPurchases(purchases)
    }

    suspend fun getAvailableItems(type: ProductQueryType): List<Purchase> = withContext(Dispatchers.IO) {
        val billingType = if (type == ProductQueryType.Subs) BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP
        queryPurchases(billingClient, billingType)
    }

    val finishTransaction: MutationFinishTransactionHandler = { purchase, isConsumable ->
        withContext(Dispatchers.IO) {
            val client = billingClient ?: throw OpenIapError.NotPrepared
            if (!client.isReady) throw OpenIapError.NotPrepared
            val token = purchase.purchaseToken.orEmpty()
            if (token.isBlank()) {
                throw OpenIapError.PurchaseFailed
            }

            val result = if (isConsumable == true) {
                val params = ConsumeParams.newBuilder().setPurchaseToken(token).build()
                suspendCancellableCoroutine<BillingResult> { continuation ->
                    client.consumeAsync(params) { outcome, _ ->
                        if (continuation.isActive) continuation.resume(outcome)
                    }
                }
            } else {
                val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(token).build()
                suspendCancellableCoroutine<BillingResult> { continuation ->
                    client.acknowledgePurchase(params) { outcome ->
                        if (continuation.isActive) continuation.resume(outcome)
                    }
                }
            }

            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                throw OpenIapError.PurchaseFailed
            }
        }
    }

    val acknowledgePurchaseAndroid: MutationAcknowledgePurchaseAndroidHandler = { purchaseToken ->
        withContext(Dispatchers.IO) {
            val client = billingClient ?: throw OpenIapError.NotPrepared
            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build()
            suspendCancellableCoroutine<Boolean> { continuation ->
                client.acknowledgePurchase(params) { result ->
                    if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                        OpenIapLog.w("Failed to acknowledge purchase: ${result.debugMessage}", TAG)
                        if (continuation.isActive) continuation.resume(false)
                    } else if (continuation.isActive) {
                        continuation.resume(true)
                    }
                }
            }
        }
    }

    val consumePurchaseAndroid: MutationConsumePurchaseAndroidHandler = { purchaseToken ->
        withContext(Dispatchers.IO) {
            val client = billingClient ?: throw OpenIapError.NotPrepared
            val params = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
            suspendCancellableCoroutine<Boolean> { continuation ->
                client.consumeAsync(params) { result, _ ->
                    if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                        OpenIapLog.w("Failed to consume purchase: ${result.debugMessage}", TAG)
                        if (continuation.isActive) continuation.resume(false)
                    } else if (continuation.isActive) {
                        continuation.resume(true)
                    }
                }
            }
        }
    }

    val deepLinkToSubscriptions: MutationDeepLinkToSubscriptionsHandler = { options ->
        val pkg = options?.packageNameAndroid ?: context.packageName
        val uri = if (!options?.skuAndroid.isNullOrBlank()) {
            Uri.parse("https://play.google.com/store/account/subscriptions?sku=${options!!.skuAndroid}&package=$pkg")
        } else {
            Uri.parse("https://play.google.com/store/account/subscriptions?package=$pkg")
        }
        val intent = Intent(Intent.ACTION_VIEW, uri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
    }

    val restorePurchases: MutationRestorePurchasesHandler = {
        withContext(Dispatchers.IO) {
            restorePurchasesHelper(billingClient)
            Unit
        }
    }

    val validateReceipt: MutationValidateReceiptHandler = { throw OpenIapError.NotSupported }

    private val purchaseError: SubscriptionPurchaseErrorHandler = {
        onPurchaseError(this::addPurchaseErrorListener, this::removePurchaseErrorListener)
    }

    private val purchaseUpdated: SubscriptionPurchaseUpdatedHandler = {
        onPurchaseUpdated(this::addPurchaseUpdateListener, this::removePurchaseUpdateListener)
    }

    val queryHandlers: QueryHandlers = QueryHandlers(
        fetchProducts = fetchProducts,
        getActiveSubscriptions = getActiveSubscriptions,
        getAvailablePurchases = getAvailablePurchases,
        getStorefrontIOS = { getStorefront() },
        hasActiveSubscriptions = hasActiveSubscriptions
    )

    val mutationHandlers: MutationHandlers = MutationHandlers(
        acknowledgePurchaseAndroid = acknowledgePurchaseAndroid,
        consumePurchaseAndroid = consumePurchaseAndroid,
        deepLinkToSubscriptions = deepLinkToSubscriptions,
        endConnection = endConnection,
        finishTransaction = finishTransaction,
        initConnection = initConnection,
        requestPurchase = requestPurchase,
        restorePurchases = restorePurchases,
        validateReceipt = validateReceipt
    )

    val subscriptionHandlers: SubscriptionHandlers = SubscriptionHandlers(
        purchaseError = purchaseError,
        purchaseUpdated = purchaseUpdated
    )

    init {
        buildBillingClient()
    }

    suspend fun getStorefront() = withContext(Dispatchers.IO) {
        val client = billingClient ?: return@withContext ""
        suspendCancellableCoroutine { continuation ->
            runCatching {
                client.getBillingConfigAsync(
                    GetBillingConfigParams.newBuilder().build(),
                    BillingConfigResponseListener { result: BillingResult, config: BillingConfig? ->
                        val code = if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            config?.countryCode.orEmpty()
                        } else ""
                        if (continuation.isActive) continuation.resume(code)
                    }
                )
            }.onFailure { error ->
                OpenIapLog.w("getStorefront failed: ${error.message}", TAG)
                if (continuation.isActive) continuation.resume("")
            }
        }
    }

    fun addPurchaseUpdateListener(listener: OpenIapPurchaseUpdateListener) {
        purchaseUpdateListeners.add(listener)
    }

    fun removePurchaseUpdateListener(listener: OpenIapPurchaseUpdateListener) {
        purchaseUpdateListeners.remove(listener)
    }

    fun addPurchaseErrorListener(listener: OpenIapPurchaseErrorListener) {
        purchaseErrorListeners.add(listener)
    }

    fun removePurchaseErrorListener(listener: OpenIapPurchaseErrorListener) {
        purchaseErrorListeners.remove(listener)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<BillingPurchase>?) {
        Log.d(TAG, "onPurchasesUpdated: code=${billingResult.responseCode} msg=${billingResult.debugMessage} count=${purchases?.size ?: 0}")
        purchases?.forEachIndexed { index, purchase ->
            Log.d(
                TAG,
                "[Purchase $index] token=${purchase.purchaseToken} orderId=${purchase.orderId} state=${purchase.purchaseState} autoRenew=${purchase.isAutoRenewing} acknowledged=${purchase.isAcknowledged} products=${purchase.products}"
            )
        }

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            val mapped = purchases.map { purchase ->
                val productType = if (purchase.products.any { it.contains("subs") }) BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP
                purchase.toPurchase(productType)
            }
            Log.d(TAG, "Mapped purchases=${gson.toJson(mapped)}")
            mapped.forEach { converted ->
                purchaseUpdateListeners.forEach { listener ->
                    runCatching { listener.onPurchaseUpdated(converted) }
                }
            }
            currentPurchaseCallback?.invoke(Result.success(mapped))
        } else {
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    val err = OpenIapError.UserCancelled
                    purchaseErrorListeners.forEach { listener -> runCatching { listener.onPurchaseError(err) } }
                    currentPurchaseCallback?.invoke(Result.success(emptyList()))
                }
                else -> {
                    val error = OpenIapError.fromBillingResponseCode(
                        billingResult.responseCode,
                        billingResult.debugMessage
                    )
                    OpenIapLog.w("Purchase failed: code=${billingResult.responseCode} msg=${error.message}", TAG)
                    purchaseErrorListeners.forEach { listener -> runCatching { listener.onPurchaseError(error) } }
                    currentPurchaseCallback?.invoke(Result.success(emptyList()))
                }
            }
        }
        currentPurchaseCallback = null
    }

    private fun buildBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .enableAutoServiceReconnection()
            .build()
    }

    private fun initBillingClient(
        onSuccess: (BillingClient) -> Unit,
        onFailure: (Throwable?) -> Unit = {}
    ) {
        val availability = GoogleApiAvailability.getInstance()
        if (availability.isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS) {
            val error = IllegalStateException("Google Play Services are not available on this device")
            onFailure(error)
            return
        }

        if (billingClient == null) {
            buildBillingClient()
        }

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    val message = billingResult.debugMessage ?: "Billing setup failed"
                    OpenIapLog.w(message, TAG)
                    onFailure(IllegalStateException(message))
                    return
                }
                billingClient?.let(onSuccess)
            }

            override fun onBillingServiceDisconnected() {
                Log.i(TAG, "Billing service disconnected")
            }
        })
    }

    fun setActivity(activity: Activity?) {
        currentActivityRef = activity?.let { WeakReference(it) }
    }
}
