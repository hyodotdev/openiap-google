package dev.hyo.openiap.store

import android.app.Activity
import android.content.Context
import dev.hyo.openiap.OpenIapModule
import dev.hyo.openiap.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * OpenIapStore (Android)
 * Convenience store that wraps OpenIapModule and provides spec-aligned, suspend APIs
 * with observable StateFlows for UI layers (Compose/XML) to consume.
 */
class OpenIapStore(context: Context) {
    private val module = OpenIapModule(context)

    // Public state
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _products = MutableStateFlow<List<OpenIapProduct>>(emptyList())
    val products: StateFlow<List<OpenIapProduct>> = _products.asStateFlow()

    private val _availablePurchases = MutableStateFlow<List<OpenIapPurchase>>(emptyList())
    val availablePurchases: StateFlow<List<OpenIapPurchase>> = _availablePurchases.asStateFlow()

    private val _currentPurchase = MutableStateFlow<OpenIapPurchase?>(null)
    val currentPurchase: StateFlow<OpenIapPurchase?> = _currentPurchase.asStateFlow()

    private val _status = MutableStateFlow(IapStatus())
    val status: StateFlow<IapStatus> = _status.asStateFlow()

    // Expose a way to set the current Activity for purchase flows
    fun setActivity(activity: Activity?) {
        module.setActivity(activity)
    }

    // -------------------------------------------------------------------------
    // Connection Management
    // -------------------------------------------------------------------------
    suspend fun initConnection(): Boolean {
        setLoading(true)
        return try {
            val ok = module.initConnection()
            _isConnected.value = ok
            ok
        } catch (e: Exception) {
            setError(e.message)
            throw e
        } finally {
            setLoading(false)
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
        setLoading(true)
        return try {
            val result = module.fetchProducts(ProductRequest(skus = skus, type = type))
            _products.value = result
            result
        } catch (e: Exception) {
            setError(e.message)
            throw e
        } finally {
            setLoading(false)
        }
    }

    // -------------------------------------------------------------------------
    // Purchases / Restore
    // -------------------------------------------------------------------------
    suspend fun getAvailablePurchases(options: PurchaseOptions? = null): List<OpenIapPurchase> {
        setLoading(true)
        return try {
            val result = module.getAvailablePurchases(options)
            _availablePurchases.value = result
            result
        } catch (e: Exception) {
            setError(e.message)
            throw e
        } finally {
            setLoading(false)
        }
    }

    suspend fun restorePurchases(): List<OpenIapPurchase> {
        return getAvailablePurchases()
    }

    // -------------------------------------------------------------------------
    // Purchase Flow
    // -------------------------------------------------------------------------
    suspend fun requestPurchase(
        params: RequestPurchaseAndroidProps,
        type: ProductRequest.ProductRequestType = ProductRequest.ProductRequestType.INAPP
    ): OpenIapPurchase? {
        val skuForStatus = params.skus.firstOrNull()
        if (skuForStatus != null) addPurchasing(skuForStatus)
        return try {
            val purchase = module.requestPurchase(params, type)
            _currentPurchase.value = purchase
            purchase
        } catch (e: Exception) {
            setError(e.message)
            throw e
        } finally {
            if (skuForStatus != null) removePurchasing(skuForStatus)
        }
    }

    suspend fun finishTransaction(
        purchase: OpenIapPurchase,
        isConsumable: Boolean = false
    ): Boolean {
        return try {
            val result = module.finishTransaction(
                FinishTransactionParams(purchase = purchase, isConsumable = isConsumable)
            )
            result.responseCode == 0
        } catch (e: Exception) {
            setError(e.message)
            throw e
        }
    }

    // -------------------------------------------------------------------------
    // Status helpers
    // -------------------------------------------------------------------------
    private fun setLoading(loading: Boolean) {
        _status.value = _status.value.copy(isLoading = loading)
    }

    private fun setError(message: String?) {
        _status.value = _status.value.copy(error = message)
    }

    private fun addPurchasing(productId: String) {
        val set = _status.value.isPurchasing.toMutableSet()
        set.add(productId)
        _status.value = _status.value.copy(isPurchasing = set)
    }

    private fun removePurchasing(productId: String) {
        val set = _status.value.isPurchasing.toMutableSet()
        set.remove(productId)
        _status.value = _status.value.copy(isPurchasing = set)
    }
}

data class IapStatus(
    val isLoading: Boolean = false,
    val isPurchasing: Set<String> = emptySet(),
    val error: String? = null
) {
    fun isPurchasing(productId: String) = isPurchasing.contains(productId)
}
