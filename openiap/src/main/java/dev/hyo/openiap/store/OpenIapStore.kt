package dev.hyo.openiap.store

import dev.hyo.openiap.ActiveSubscription
import dev.hyo.openiap.DeepLinkOptions
import dev.hyo.openiap.FetchProductsResult
import dev.hyo.openiap.FetchProductsResultProducts
import dev.hyo.openiap.FetchProductsResultSubscriptions
import dev.hyo.openiap.Product
import dev.hyo.openiap.ProductAndroid
import dev.hyo.openiap.ProductQueryType
import dev.hyo.openiap.ProductRequest
import dev.hyo.openiap.ProductSubscription
import dev.hyo.openiap.ProductSubscriptionAndroid
import dev.hyo.openiap.Purchase
import dev.hyo.openiap.PurchaseAndroid
import dev.hyo.openiap.PurchaseInput
import dev.hyo.openiap.PurchaseOptions
import dev.hyo.openiap.RequestPurchaseAndroidProps
import dev.hyo.openiap.RequestPurchaseProps
import dev.hyo.openiap.RequestPurchasePropsByPlatforms
import dev.hyo.openiap.RequestSubscriptionAndroidProps
import dev.hyo.openiap.RequestSubscriptionPropsByPlatforms
import dev.hyo.openiap.RequestPurchaseResultPurchase
import dev.hyo.openiap.RequestPurchaseResultPurchases
import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import dev.hyo.openiap.OpenIapError
import dev.hyo.openiap.OpenIapModule
import dev.hyo.openiap.listener.OpenIapPurchaseErrorListener
import dev.hyo.openiap.listener.OpenIapPurchaseUpdateListener
import dev.hyo.openiap.utils.toProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * OpenIapStore (Android)
 * Convenience store that wraps OpenIapModule and provides spec-aligned, suspend APIs
 * with observable StateFlows for UI layers (Compose/XML) to consume.
 */
class OpenIapStore(private val module: OpenIapModule) {
    constructor(context: Context) : this(OpenIapModule(context))

    // Public state
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    // Backwards-compat alias for example app
    val connectionStatus: StateFlow<Boolean> get() = isConnected

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _subscriptions = MutableStateFlow<List<ProductSubscription>>(emptyList())
    val subscriptions: StateFlow<List<ProductSubscription>> = _subscriptions.asStateFlow()

    private val _availablePurchases = MutableStateFlow<List<Purchase>>(emptyList())
    val availablePurchases: StateFlow<List<Purchase>> = _availablePurchases.asStateFlow()

    private val _currentPurchase = MutableStateFlow<Purchase?>(null)
    val currentPurchase: StateFlow<Purchase?> = _currentPurchase.asStateFlow()

    private val _status = MutableStateFlow(IapStatus())
    val status: StateFlow<IapStatus> = _status.asStateFlow()

    // Prevent duplicate finishing/consuming of the same purchase token
    private val processedPurchaseTokens = mutableSetOf<String>()

    // Keep listener references to support proper removal
    private var pendingRequestProductId: String? = null

    private val purchaseUpdateListener = OpenIapPurchaseUpdateListener { purchase ->
        _currentPurchase.value = purchase
        setStatusMessage(
            message = "Purchase successful",
            status = PurchaseResultStatus.Success,
            productId = purchase.productId,
            transactionId = purchase.id
        )
        _status.value = _status.value.copy(lastError = null)
        pendingRequestProductId = null
    }
    private val purchaseErrorListener = OpenIapPurchaseErrorListener { error ->
        if (error is OpenIapError.UserCancelled || error is OpenIapError.PurchaseCancelled) {
            val message = OpenIapError.defaultMessage(OpenIapError.UserCancelled.CODE)
            setStatusMessage(
                message = message,
                status = PurchaseResultStatus.Info,
                productId = pendingRequestProductId
            )
            _status.value = _status.value.copy(lastError = null)
            pendingRequestProductId = null
            return@OpenIapPurchaseErrorListener
        }
        val code = OpenIapError.toCode(error)
        val message = error.message?.takeIf { it.isNotBlank() } ?: OpenIapError.defaultMessage(code)
        setStatusMessage(
            message = message,
            status = PurchaseResultStatus.Error,
            productId = pendingRequestProductId,
            code = code
        )
        _status.value = _status.value.copy(
            lastError = ErrorData(
                code = code,
                message = message
            )
        )
        pendingRequestProductId = null
    }

    // Expose a way to set the current Activity for purchase flows
    fun setActivity(activity: Activity?) {
        (module as? OpenIapModule)?.setActivity(activity)
    }

    init {
        module.addPurchaseUpdateListener(purchaseUpdateListener)
        module.addPurchaseErrorListener(purchaseErrorListener)
    }

    /**
     * Clear listeners and transient state. Call when the screen is disposed.
     */
    fun clear() {
        module.removePurchaseUpdateListener(purchaseUpdateListener)
        module.removePurchaseErrorListener(purchaseErrorListener)
        processedPurchaseTokens.clear()
        pendingRequestProductId = null
    }

    // -------------------------------------------------------------------------
    // Connection Management
    // -------------------------------------------------------------------------
    suspend fun initConnection(): Boolean {
        setLoading { it.initConnection = true }
        return try {
            val ok = module.initConnection()
            _isConnected.value = ok
            ok
        } catch (e: Exception) {
            setError(e.message)
            throw e
        } finally {
            setLoading { it.initConnection = false }
        }
    }

    suspend fun endConnection(): Boolean {
        return try {
            val ok = module.endConnection()
            _isConnected.value = false
            ok
        } catch (e: Exception) {
            setError(e.message)
            throw e
        }
    }

    // -------------------------------------------------------------------------
    // Product Management
    // -------------------------------------------------------------------------
    suspend fun fetchProducts(
        skus: List<String>,
        type: ProductQueryType = ProductQueryType.All
    ): FetchProductsResult {
        setLoading { it.fetchProducts = true }
        return try {
            val result = module.fetchProducts(ProductRequest(skus = skus, type = type))
            when (result) {
                is FetchProductsResultProducts -> {
                    _products.value = result.value.orEmpty()
                    _subscriptions.value = emptyList()
                }
                is FetchProductsResultSubscriptions -> {
                    val subs = result.value.orEmpty()
                    _subscriptions.value = subs
                    _products.value = subs.mapNotNull { subscription ->
                        (subscription as? ProductSubscriptionAndroid)?.toProduct()
                    }
                }
            }
            result
        } catch (e: Exception) {
            setError(e.message)
            throw e
        } finally {
            setLoading { it.fetchProducts = false }
        }
    }

    // -------------------------------------------------------------------------
    // Purchases / Restore
    // -------------------------------------------------------------------------
    suspend fun getAvailablePurchases(options: PurchaseOptions? = null): List<Purchase> {
        setLoading { it.restorePurchases = true }
        return try {
            val result = module.getAvailablePurchases(options)
            _availablePurchases.value = result
            result
        } catch (e: Exception) {
            setError(e.message)
            throw e
        } finally {
            setLoading { it.restorePurchases = false }
        }
    }

    suspend fun restorePurchases(): List<Purchase> = getAvailablePurchases()

    suspend fun loadPurchases(): List<Purchase> = getAvailablePurchases()

    // -------------------------------------------------------------------------
    // Purchase Flow
    // -------------------------------------------------------------------------
    suspend fun requestPurchase(
        skus: List<String>,
        type: ProductQueryType = ProductQueryType.InApp
    ): List<Purchase> {
        val skuForStatus = skus.firstOrNull()
        if (skuForStatus != null) {
            addPurchasing(skuForStatus)
            pendingRequestProductId = skuForStatus
        }
        return try {
            val request = buildRequestPurchaseProps(skus, type)
            when (val result = module.requestPurchase(request)) {
                is RequestPurchaseResultPurchases -> result.value.orEmpty()
                is RequestPurchaseResultPurchase -> result.value?.let(::listOf).orEmpty()
                else -> emptyList()
            }
        } finally {
            if (skuForStatus != null) removePurchasing(skuForStatus)
        }
    }

    suspend fun finishTransaction(
        purchase: Purchase,
        isConsumable: Boolean = false
    ): Boolean {
        val token = purchase.purchaseToken
        if ((purchase is PurchaseAndroid && purchase.isAcknowledgedAndroid == true) || (token != null && processedPurchaseTokens.contains(token))) {
            return true
        }
        return try {
            module.finishTransaction(purchase.toInput(), isConsumable)
            if (token != null) processedPurchaseTokens.add(token)
            true
        } catch (e: Exception) {
            setError(e.message)
            throw e
        }
    }

    // -------------------------------------------------------------------------
    // Subscriptions
    // -------------------------------------------------------------------------
    suspend fun getActiveSubscriptions(subscriptionIds: List<String>? = null): List<ActiveSubscription> =
        module.getActiveSubscriptions(subscriptionIds)

    suspend fun hasActiveSubscriptions(subscriptionIds: List<String>? = null): Boolean =
        module.hasActiveSubscriptions(subscriptionIds)

    suspend fun deepLinkToSubscriptions(options: DeepLinkOptions) = module.deepLinkToSubscriptions(options)

    // -------------------------------------------------------------------------
    // Event listeners passthrough
    // -------------------------------------------------------------------------
    fun addPurchaseUpdateListener(listener: OpenIapPurchaseUpdateListener) = module.addPurchaseUpdateListener(listener)
    fun removePurchaseUpdateListener(listener: OpenIapPurchaseUpdateListener) = module.removePurchaseUpdateListener(listener)
    fun addPurchaseErrorListener(listener: OpenIapPurchaseErrorListener) = module.addPurchaseErrorListener(listener)
    fun removePurchaseErrorListener(listener: OpenIapPurchaseErrorListener) = module.removePurchaseErrorListener(listener)

    // -------------------------------------------------------------------------
    // Status helpers
    // -------------------------------------------------------------------------
    private fun setLoading(block: (LoadingStates) -> Unit) {
        val current = _status.value
        val loading = current.loadings.copy().apply { block(this) }
        _status.value = current.copy(loadings = loading)
    }

    private fun setError(message: String?) {
        val msg = message ?: "Operation failed"
        setStatusMessage(msg, PurchaseResultStatus.Error)
        _status.value = _status.value.copy(lastError = message?.let {
            ErrorData(code = "ERROR", message = it)
        })
    }

    private fun setStatusMessage(
        message: String,
        status: PurchaseResultStatus,
        productId: String? = null,
        transactionId: String? = null,
        code: String? = null
    ) {
        _status.value = _status.value.copy(
            lastPurchaseResult = PurchaseResultData(
                productId = productId,
                transactionId = transactionId,
                message = message,
                status = status,
                code = code
            )
        )
    }

    fun postStatusMessage(
        message: String,
        status: PurchaseResultStatus,
        productId: String? = null
    ) {
        setStatusMessage(message, status, productId)
        _status.value = _status.value.copy(
            lastError = if (status == PurchaseResultStatus.Error) {
                ErrorData(code = "ERROR", message = message)
            } else {
                null
            }
        )
    }

    fun clearStatusMessage() {
        _status.value = _status.value.copy(lastPurchaseResult = null)
    }

    private fun addPurchasing(productId: String) {
        val current = _status.value
        val set = current.loadings.purchasing.toMutableSet()
        set.add(productId)
        _status.value = current.copy(loadings = current.loadings.copy(purchasing = set))
    }

    private fun removePurchasing(productId: String) {
        val current = _status.value
        val set = current.loadings.purchasing.toMutableSet()
        set.remove(productId)
        _status.value = current.copy(loadings = current.loadings.copy(purchasing = set))
    }

    private fun buildRequestPurchaseProps(skus: List<String>, type: ProductQueryType): RequestPurchaseProps {
        return when (type) {
            ProductQueryType.InApp -> {
                val android = RequestPurchaseAndroidProps(
                    isOfferPersonalized = null,
                    obfuscatedAccountIdAndroid = null,
                    obfuscatedProfileIdAndroid = null,
                    skus = skus
                )
                RequestPurchaseProps(
                    request = RequestPurchaseProps.Request.Purchase(
                        RequestPurchasePropsByPlatforms(android = android)
                    ),
                    type = ProductQueryType.InApp
                )
            }
            ProductQueryType.Subs -> {
                val android = RequestSubscriptionAndroidProps(
                    isOfferPersonalized = null,
                    obfuscatedAccountIdAndroid = null,
                    obfuscatedProfileIdAndroid = null,
                    purchaseTokenAndroid = null,
                    replacementModeAndroid = null,
                    skus = skus,
                    subscriptionOffers = null
                )
                RequestPurchaseProps(
                    request = RequestPurchaseProps.Request.Subscription(
                        RequestSubscriptionPropsByPlatforms(android = android)
                    ),
                    type = ProductQueryType.Subs
                )
            }
            ProductQueryType.All -> throw IllegalArgumentException("type must be InApp or Subs when requesting a purchase")
        }
    }

    private fun Purchase.toInput(): PurchaseInput = when (this) {
        is PurchaseAndroid -> PurchaseInput(
            id = id,
            ids = ids,
            isAutoRenewing = isAutoRenewing,
            platform = platform,
            productId = productId,
            purchaseState = purchaseState,
            purchaseToken = purchaseToken,
            quantity = quantity,
            transactionDate = transactionDate
        )
        else -> throw UnsupportedOperationException("Only Android purchases are supported on this platform")
    }
}

// -----------------------------------------------------------------------------
// Status types (aligned with openiap-apple)
// -----------------------------------------------------------------------------
data class IapStatus(
    val loadings: LoadingStates = LoadingStates(),
    val lastPurchaseResult: PurchaseResultData? = null,
    val lastError: ErrorData? = null,
    val currentOperation: IapOperation? = null,
    val operationHistory: List<IapOperation> = emptyList()
) {
    fun isPurchasing(productId: String) = loadings.purchasing.contains(productId)
    val isLoading: Boolean
        get() = loadings.initConnection || loadings.fetchProducts || loadings.restorePurchases || loadings.purchasing.isNotEmpty()
}

data class LoadingStates(
    var initConnection: Boolean = false,
    var fetchProducts: Boolean = false,
    var restorePurchases: Boolean = false,
    var purchasing: Set<String> = emptySet()
)

data class PurchaseResultData(
    val productId: String?,
    val transactionId: String?,
    val message: String,
    val status: PurchaseResultStatus = PurchaseResultStatus.Success,
    val code: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class PurchaseResultStatus { Success, Info, Error }

data class ErrorData(
    val code: String,
    val message: String,
    val productId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class IapOperation(
    val type: IapOperationType,
    val productId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val result: IapOperationResult? = null
)

enum class IapOperationType {
    InitConnection,
    EndConnection,
    FetchProducts,
    RequestPurchase,
    FinishTransaction,
    RestorePurchases,
    ValidateReceipt,
}
sealed class IapOperationResult {
    object Success : IapOperationResult()
    data class Failure(val message: String) : IapOperationResult()
    object Cancelled : IapOperationResult()
}
