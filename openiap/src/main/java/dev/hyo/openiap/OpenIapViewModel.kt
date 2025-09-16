package dev.hyo.openiap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.hyo.openiap.models.*
import dev.hyo.openiap.store.OpenIapStore
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Android ViewModel wrapper around OpenIapStore for easy integration
 */
class OpenIapViewModel(app: Application) : AndroidViewModel(app) {
    private val store = OpenIapStore(app.applicationContext)

    val isConnected: StateFlow<Boolean> = store.isConnected
    val products = store.products
    val availablePurchases = store.availablePurchases
    val status = store.status

    fun initConnection() { viewModelScope.launch { runCatching { store.initConnection() } } }
    fun endConnection() { viewModelScope.launch { runCatching { store.endConnection() } } }

    fun fetchProducts(skus: List<String>, type: ProductRequest.ProductRequestType = ProductRequest.ProductRequestType.ALL) {
        viewModelScope.launch { runCatching { store.fetchProducts(skus, type) } }
    }

    fun restorePurchases() { viewModelScope.launch { runCatching { store.restorePurchases() } } }

    fun requestPurchase(params: RequestPurchaseParams, type: ProductRequest.ProductRequestType = ProductRequest.ProductRequestType.INAPP) {
        viewModelScope.launch { runCatching { store.requestPurchase(params, type) } }
    }
}
