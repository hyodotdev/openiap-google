package dev.hyo.martie.viewmodels

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.hyo.openiap.OpenIapModule
import dev.hyo.openiap.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IapStatus(
    val isLoading: Boolean = false,
    val isPurchasing: Set<String> = emptySet(),
    val error: String? = null
) {
    fun isPurchasing(productId: String) = isPurchasing.contains(productId)
}

class OpenIapStore(application: Application) : AndroidViewModel(application) {
    private val openIap: OpenIapModule = OpenIapModule(application.applicationContext)
    
    private val _products = MutableStateFlow<List<OpenIapProduct>>(emptyList())
    val products: StateFlow<List<OpenIapProduct>> = _products.asStateFlow()
    
    private val _purchases = MutableStateFlow<List<OpenIapPurchase>>(emptyList())
    val purchases: StateFlow<List<OpenIapPurchase>> = _purchases.asStateFlow()
    
    private val _status = MutableStateFlow(IapStatus())
    val status: StateFlow<IapStatus> = _status.asStateFlow()
    
    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()

    // Provide current Activity for purchase flows
    fun setActivity(activity: Activity?) {
        openIap.setActivity(activity)
    }
    
    // initialize() no longer required; ViewModel constructs module with Application context.
    fun initialize(@Suppress("UNUSED_PARAMETER") openIap: OpenIapModule, autoConnect: Boolean = true) {
        if (autoConnect) connect()
    }
    
    fun connect(onConnected: (() -> Unit)? = null) {
        viewModelScope.launch {
            _status.value = _status.value.copy(isLoading = true)
            try {
                val connected = openIap.initConnection()
                _connectionStatus.value = connected
                if (connected) {
                    onConnected?.invoke()
                }
                _status.value = _status.value.copy(isLoading = false)
            } catch (e: Exception) {
                _status.value = _status.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    // Swift-compatible: initConnection()
    suspend fun initConnection(): Boolean {
        _status.value = _status.value.copy(isLoading = true, error = null)
        return try {
            val connected = openIap.initConnection()
            _connectionStatus.value = connected
            connected
        } catch (e: Exception) {
            _status.value = _status.value.copy(error = e.message)
            throw e
        } finally {
            _status.value = _status.value.copy(isLoading = false)
        }
    }

    // Swift-compatible: endConnection()
    suspend fun endConnection(): Boolean {
        return try {
            val result = openIap.endConnection()
            _connectionStatus.value = false
            result
        } catch (e: Exception) {
            _status.value = _status.value.copy(error = e.message)
            throw e
        }
    }
    
    fun loadProducts(
        productIds: List<String> = emptyList(),
        type: ProductRequest.ProductRequestType = ProductRequest.ProductRequestType.INAPP
    ) {
        viewModelScope.launch {
            _status.value = _status.value.copy(isLoading = true, error = null)
            try {
                if (productIds.isEmpty()) {
                    _status.value = _status.value.copy(isLoading = false)
                    return@launch
                }
                println("Loading products: $productIds, type: $type")
                val fetchedProducts = openIap.fetchProducts(
                    ProductRequest(
                        skus = productIds,
                        type = type
                    )
                )
                println("Loaded ${fetchedProducts.size} products:")
                fetchedProducts.forEach { product ->
                    println("  - ${product.id}: type=${product.type}")
                }
                _products.value = fetchedProducts
            } catch (e: Exception) {
                println("Error loading products: ${e.message}")
                _status.value = _status.value.copy(error = e.message)
            } finally {
                _status.value = _status.value.copy(isLoading = false)
            }
        }
    }

    // Swift-compatible: fetchProducts(skus:type)
    suspend fun fetchProducts(
        skus: List<String>,
        type: ProductRequest.ProductRequestType = ProductRequest.ProductRequestType.ALL
    ): List<OpenIapProduct> {
        _status.value = _status.value.copy(isLoading = true, error = null)
        return try {
            val fetchedProducts = openIap.fetchProducts(
                ProductRequest(
                    skus = skus,
                    type = type
                )
            )
            _products.value = fetchedProducts
            fetchedProducts
        } catch (e: Exception) {
            _status.value = _status.value.copy(error = e.message)
            throw e
        } finally {
            _status.value = _status.value.copy(isLoading = false)
        }
    }
    
    fun loadPurchases() {
        viewModelScope.launch {
            try {
                val fetchedPurchases = openIap.getAvailablePurchases()
                _purchases.value = fetchedPurchases
            } catch (e: Exception) {
                _status.value = _status.value.copy(error = e.message)
            }
        }
    }

    // Swift-compatible: getAvailablePurchases(options)
    suspend fun getAvailablePurchases(options: PurchaseOptions? = null): List<OpenIapPurchase> {
        _status.value = _status.value.copy(isLoading = true, error = null)
        return try {
            val fetched = openIap.getAvailablePurchases(options)
            _purchases.value = fetched
            fetched
        } catch (e: Exception) {
            _status.value = _status.value.copy(error = e.message)
            throw e
        } finally {
            _status.value = _status.value.copy(isLoading = false)
        }
    }
    
    fun purchaseProduct(product: OpenIapProduct, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val updatedPurchasing = _status.value.isPurchasing.toMutableSet()
            updatedPurchasing.add(product.id)
            _status.value = _status.value.copy(isPurchasing = updatedPurchasing)
            
            try {
                val purchaseType = if (product.type == OpenIapProduct.ProductType.SUBS) {
                    ProductRequest.ProductRequestType.SUBS
                } else {
                    ProductRequest.ProductRequestType.INAPP
                }
                
                println("Attempting to purchase product: ${product.id}, type: ${product.type}, requestType: $purchaseType")
                
                val purchase = openIap.requestPurchase(
                    request = RequestPurchaseAndroidProps(
                        skus = listOf(product.id)
                    ),
                    type = purchaseType
                )
                
                println("Purchase result for ${product.id}: ${if (purchase != null) "SUCCESS" else "NULL"}")
                
                if (purchase != null) {
                    try {
                        // TODO: validateReceipt - Send purchase to your server for validation
                        // val validationResult = yourServerApi.validateReceipt(purchase.purchaseToken, purchase.productId)
                        // if (!validationResult.isValid) {
                        //     onResult(false, "Purchase validation failed")
                        //     return@launch
                        // }
                        
                        // Handle different product types
                        when (product.type) {
                            OpenIapProduct.ProductType.SUBS -> {
                                // For subscriptions: acknowledge the purchase (not consumable)
                                println("Finishing subscription transaction for ${product.id}")
                                openIap.finishTransaction(
                                    FinishTransactionParams(
                                        purchase = purchase,
                                        isConsumable = false
                                    )
                                )
                            }
                            OpenIapProduct.ProductType.INAPP -> {
                                // For in-app products: consume if consumable, acknowledge if not
                                val isConsumable = product.id.contains("consumable", ignoreCase = true) ||
                                                 product.id.contains("bulb", ignoreCase = true)
                                println("Finishing ${if (isConsumable) "consumable" else "non-consumable"} transaction for ${product.id}")
                                openIap.finishTransaction(
                                    FinishTransactionParams(
                                        purchase = purchase,
                                        isConsumable = isConsumable
                                    )
                                )
                            }
                        }
                        
                        // Reload purchases after successful transaction
                        loadPurchases()
                        
                        onResult(true, "Purchase successful: ${product.title}")
                    } catch (finishError: Exception) {
                        println("Error finishing transaction: ${finishError.message}")
                        // Even if finish fails, the purchase was successful
                        onResult(true, "Purchase successful but finish failed: ${product.title}")
                    }
                } else {
                    onResult(false, "Purchase cancelled")
                }
            } catch (e: Exception) {
                println("Purchase failed for ${product.id}: ${e.message}")
                e.printStackTrace()
                onResult(false, "Purchase failed: ${e.message}")
            } finally {
                val updatedPurchasing = _status.value.isPurchasing.toMutableSet()
                updatedPurchasing.remove(product.id)
                _status.value = _status.value.copy(isPurchasing = updatedPurchasing)
            }
        }
    }

    // Swift-compatible: requestPurchase(params)
    suspend fun requestPurchase(
        params: RequestPurchaseAndroidProps,
        type: ProductRequest.ProductRequestType = ProductRequest.ProductRequestType.INAPP
    ): OpenIapPurchase? {
        val skuForStatus = params.skus.firstOrNull()
        if (skuForStatus != null) {
            val updated = _status.value.isPurchasing.toMutableSet()
            updated.add(skuForStatus)
            _status.value = _status.value.copy(isPurchasing = updated, error = null)
        }
        return try {
            openIap.requestPurchase(params, type)
        } catch (e: Exception) {
            _status.value = _status.value.copy(error = e.message)
            throw e
        } finally {
            if (skuForStatus != null) {
                val updated = _status.value.isPurchasing.toMutableSet()
                updated.remove(skuForStatus)
                _status.value = _status.value.copy(isPurchasing = updated)
            }
        }
    }
    
    fun restorePurchases(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _status.value = _status.value.copy(isLoading = true)
            try {
                val restoredPurchases = openIap.getAvailablePurchases()
                _purchases.value = restoredPurchases
                onResult(true, "Restored ${restoredPurchases.size} purchases")
            } catch (e: Exception) {
                onResult(false, "Restore failed: ${e.message}")
            } finally {
                _status.value = _status.value.copy(isLoading = false)
            }
        }
    }

    // Swift-compatible: restorePurchases()
    suspend fun restorePurchases(): List<OpenIapPurchase> {
        return getAvailablePurchases()
    }
    
    fun finishPurchase(purchase: OpenIapPurchase, isConsumable: Boolean, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                // TODO: validateReceipt - Send purchase to your server for validation
                // val validationResult = yourServerApi.validateReceipt(purchase.purchaseToken, purchase.productId)
                // if (!validationResult.isValid) {
                //     onResult(false, "Receipt validation failed")
                //     return@launch
                // }
                
                println("Manually finishing transaction for ${purchase.productId}, consumable: $isConsumable")
                openIap.finishTransaction(
                    FinishTransactionParams(
                        purchase = purchase,
                        isConsumable = isConsumable
                    )
                )
                
                // Reload purchases after finishing
                loadPurchases()
                
                onResult(true, "Transaction finished successfully")
            } catch (e: Exception) {
                println("Error finishing transaction: ${e.message}")
                onResult(false, "Failed to finish transaction: ${e.message}")
            }
        }
    }

    // Swift-compatible: finishTransaction(purchase:isConsumable)
    suspend fun finishTransaction(
        purchase: OpenIapPurchase,
        isConsumable: Boolean = false
    ): Boolean {
        return try {
            val result = openIap.finishTransaction(
                FinishTransactionParams(
                    purchase = purchase,
                    isConsumable = isConsumable
                )
            )
            // BillingResponseCode.OK == 0
            result.responseCode == 0
        } catch (e: Exception) {
            _status.value = _status.value.copy(error = e.message)
            throw e
        }
    }
    
    fun disconnect() {
        viewModelScope.launch {
            try {
                openIap.endConnection()
                _connectionStatus.value = false
            } catch (e: Exception) {
                // Ignore disconnect errors
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
