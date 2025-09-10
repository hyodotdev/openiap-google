package dev.hyo.openiap.store

import android.app.Activity
import android.content.Context
import dev.hyo.openiap.OpenIapModule
import dev.hyo.openiap.OpenIapError
import dev.hyo.openiap.OpenIapProtocol
import dev.hyo.openiap.models.*
import dev.hyo.openiap.listener.OpenIapPurchaseUpdateListener
import dev.hyo.openiap.listener.OpenIapPurchaseErrorListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * OpenIapStore (Android)
 * Convenience store that wraps OpenIapModule and provides spec-aligned, suspend APIs
 * with observable StateFlows for UI layers (Compose/XML) to consume.
 */
class OpenIapStore(private val module: OpenIapProtocol) {
    constructor(context: Context) : this(OpenIapModule(context))

    // Public state
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    // Backwards-compat alias for example app
    val connectionStatus: StateFlow<Boolean> get() = isConnected

    private val _products = MutableStateFlow<List<OpenIapProduct>>(emptyList())
    val products: StateFlow<List<OpenIapProduct>> = _products.asStateFlow()

    private val _availablePurchases = MutableStateFlow<List<OpenIapPurchase>>(emptyList())
    val availablePurchases: StateFlow<List<OpenIapPurchase>> = _availablePurchases.asStateFlow()

    private val _currentPurchase = MutableStateFlow<OpenIapPurchase?>(null)
    val currentPurchase: StateFlow<OpenIapPurchase?> = _currentPurchase.asStateFlow()

    private val _status = MutableStateFlow(IapStatus())
    val status: StateFlow<IapStatus> = _status.asStateFlow()

    // Prevent duplicate finishing/consuming of the same purchase token
    private val processedPurchaseTokens = mutableSetOf<String>()

    // Keep listener references to support proper removal
    private val purchaseUpdateListener = OpenIapPurchaseUpdateListener { purchase ->
        _currentPurchase.value = purchase
        _status.value = _status.value.copy(
            lastPurchaseResult = PurchaseResultData(
                productId = purchase.productId,
                transactionId = purchase.id,
                message = "Purchase successful"
            )
        )
    }
    private val purchaseErrorListener = OpenIapPurchaseErrorListener { error ->
        _status.value = _status.value.copy(
            lastError = ErrorData(
                code = OpenIapError.toCode(error),
                message = error.message
            )
        )
    }

    // Expose a way to set the current Activity for purchase flows
    fun setActivity(activity: Activity?) {
        (module as? OpenIapModule)?.setActivity(activity)
    }

    init {
        // Wire event listeners to update Store state (event-based flow)
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
        type: ProductRequest.ProductRequestType = ProductRequest.ProductRequestType.ALL
    ): List<OpenIapProduct> {
        setLoading { it.fetchProducts = true }
        return try {
            val result = module.fetchProducts(ProductRequest(skus = skus, type = type))
            _products.value = result
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
    suspend fun getAvailablePurchases(options: PurchaseOptions? = null): List<OpenIapPurchase> {
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

    suspend fun restorePurchases(): List<OpenIapPurchase> {
        return getAvailablePurchases()
    }

    // Convenience alias to match example usage
    suspend fun loadPurchases(): List<OpenIapPurchase> = getAvailablePurchases()

    // -------------------------------------------------------------------------
    // Purchase Flow
    // -------------------------------------------------------------------------
    suspend fun requestPurchase(
        params: RequestPurchaseAndroidProps,
        type: ProductRequest.ProductRequestType = ProductRequest.ProductRequestType.INAPP
    ): List<OpenIapPurchase> {
        val skuForStatus = params.skus.firstOrNull()
        if (skuForStatus != null) addPurchasing(skuForStatus)
        return try {
            module.requestPurchase(params, type)
        } finally {
            if (skuForStatus != null) removePurchasing(skuForStatus)
        }
    }

    suspend fun finishTransaction(
        purchase: OpenIapPurchase,
        isConsumable: Boolean = false
    ): Boolean {
        // Skip if already acknowledged or processed
        val token = purchase.purchaseToken ?: purchase.purchaseTokenAndroid
        if (purchase.isAcknowledgedAndroid == true || (token != null && processedPurchaseTokens.contains(token))) {
            return true
        }
        return try {
            val result = module.finishTransaction(
                FinishTransactionParams(purchase = purchase, isConsumable = isConsumable)
            )
            val ok = result.responseCode == 0
            if (ok && token != null) processedPurchaseTokens.add(token)
            ok
        } catch (e: Exception) {
            setError(e.message)
            throw e
        }
    }

    // -------------------------------------------------------------------------
    // Subscriptions
    // -------------------------------------------------------------------------
    suspend fun getActiveSubscriptions(subscriptionIds: List<String>? = null): List<OpenIapActiveSubscription> =
        module.getActiveSubscriptions(subscriptionIds)

    suspend fun hasActiveSubscriptions(subscriptionIds: List<String>? = null): Boolean =
        module.hasActiveSubscriptions(subscriptionIds)

    suspend fun deepLinkToSubscriptions(options: DeepLinkOptions) = module.deepLinkToSubscriptions(options)

    // -------------------------------------------------------------------------
    // Event listeners
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
        _status.value = _status.value.copy(lastError = message?.let {
            ErrorData(code = "ERROR", message = it)
        })
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
    val productId: String,
    val transactionId: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

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

enum class IapOperationType { INIT_CONNECTION, END_CONNECTION, FETCH_PRODUCTS, REQUEST_PURCHASE, FINISH_TRANSACTION, RESTORE_PURCHASES, VALIDATE_RECEIPT }
sealed class IapOperationResult { object Success : IapOperationResult(); data class Failure(val message: String) : IapOperationResult(); object Cancelled : IapOperationResult() }
