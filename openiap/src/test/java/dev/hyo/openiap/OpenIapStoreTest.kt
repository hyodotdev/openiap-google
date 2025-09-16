package dev.hyo.openiap

import dev.hyo.openiap.listener.OpenIapPurchaseErrorListener
import dev.hyo.openiap.listener.OpenIapPurchaseUpdateListener
import dev.hyo.openiap.models.*
import dev.hyo.openiap.store.OpenIapStore
import org.junit.Assert.*
import org.junit.Test

class OpenIapStoreTest {

    // Fake implementation of OpenIapProtocol for unit testing
    class FakeModule : OpenIapProtocol {
        var initCalled = 0
        var endCalled = 0
        var finishCalled = 0
        var lastDeepLinkOptions: DeepLinkOptions? = null
        private val updateListeners = mutableSetOf<OpenIapPurchaseUpdateListener>()
        private val errorListeners = mutableSetOf<OpenIapPurchaseErrorListener>()

        // Configurable responses
        var productsToReturn: List<OpenIapProduct> = emptyList()
        var purchasesToReturn: List<OpenIapPurchase> = emptyList()
        var activeSubsToReturn: List<OpenIapActiveSubscription> = emptyList()
        var requestEmitsPurchases: List<OpenIapPurchase> = emptyList()

        override suspend fun initConnection(): Boolean {
            initCalled++
            return true
        }

        override suspend fun endConnection(): Boolean {
            endCalled++
            return true
        }

        override suspend fun fetchProducts(params: ProductRequest): List<OpenIapProduct> = productsToReturn

        override suspend fun getAvailablePurchases(options: PurchaseOptions?): List<OpenIapPurchase> = purchasesToReturn

        override suspend fun getAvailableItems(type: ProductRequest.ProductRequestType): List<OpenIapPurchase> = purchasesToReturn

        override suspend fun getActiveSubscriptions(subscriptionIds: List<String>?): List<OpenIapActiveSubscription> = activeSubsToReturn

        override suspend fun hasActiveSubscriptions(subscriptionIds: List<String>?): Boolean = activeSubsToReturn.isNotEmpty()

        override suspend fun requestPurchase(request: RequestPurchaseParams, type: ProductRequest.ProductRequestType): List<OpenIapPurchase> {
            // Broadcast to listeners
            requestEmitsPurchases.forEach { p ->
                updateListeners.forEach { it.onPurchaseUpdated(p) }
            }
            return requestEmitsPurchases
        }

        override suspend fun finishTransaction(params: FinishTransactionParams): PurchaseResult {
            finishCalled++
            return PurchaseResult(responseCode = 0)
        }

        override suspend fun validateReceipt(options: ReceiptValidationProps): ReceiptValidationResultAndroid? = null

        override suspend fun acknowledgePurchaseAndroid(purchaseToken: String) {}

        override suspend fun consumePurchaseAndroid(purchaseToken: String) {}

        override suspend fun deepLinkToSubscriptions(options: DeepLinkOptions) {
            lastDeepLinkOptions = options
        }

        override suspend fun getStorefront(): String = ""

        override fun addPurchaseUpdateListener(listener: OpenIapPurchaseUpdateListener) {
            updateListeners.add(listener)
        }

        override fun removePurchaseUpdateListener(listener: OpenIapPurchaseUpdateListener) {
            updateListeners.remove(listener)
        }

        override fun addPurchaseErrorListener(listener: OpenIapPurchaseErrorListener) {
            errorListeners.add(listener)
        }

        override fun removePurchaseErrorListener(listener: OpenIapPurchaseErrorListener) {
            errorListeners.remove(listener)
        }
    }

    private fun samplePurchase(token: String = "token-1", productId: String = "sku1"): OpenIapPurchase {
        return OpenIapPurchase(
            id = token,
            productId = productId,
            ids = listOf(productId),
            transactionId = "order-1",
            transactionDate = System.currentTimeMillis(),
            transactionReceipt = "{}",
            purchaseToken = token,
            platform = "android",
            quantity = 1,
            purchaseState = OpenIapPurchase.PurchaseState.PURCHASED,
            isAutoRenewing = false,
            purchaseTokenAndroid = token,
            dataAndroid = null,
            signatureAndroid = null,
            autoRenewingAndroid = false,
            isAcknowledgedAndroid = false,
            packageNameAndroid = "dev.hyo.martie",
            developerPayloadAndroid = "",
            obfuscatedAccountIdAndroid = "",
            obfuscatedProfileIdAndroid = "",
        )
    }

    @Test
    fun finishTransaction_isIdempotentByToken() = kotlinx.coroutines.test.runTest {
        val fake = FakeModule()
        val store = OpenIapStore(fake)
        val purchase = samplePurchase(token = "t-123", productId = "sku.test")

        val first = store.finishTransaction(purchase, isConsumable = false)
        val second = store.finishTransaction(purchase, isConsumable = false)

        assertTrue(first)
        assertTrue(second)
        assertEquals(1, fake.finishCalled)
    }

    @Test
    fun requestPurchase_emitsListenerUpdates_andReturnsList() = kotlinx.coroutines.test.runTest {
        val fake = FakeModule()
        val store = OpenIapStore(fake)
        val emitted = samplePurchase(token = "t-1", productId = "sku1")
        fake.requestEmitsPurchases = listOf(emitted)

        val result = store.requestPurchase(
            RequestPurchaseParams(skus = listOf("sku1")),
            ProductRequest.ProductRequestType.INAPP
        )

        assertEquals(1, result.size)
        assertEquals("sku1", result.first().productId)
        assertEquals("sku1", store.currentPurchase.value?.productId)
    }

    @Test
    fun deepLink_isDelegatedToModule() = kotlinx.coroutines.test.runTest {
        val fake = FakeModule()
        val store = OpenIapStore(fake)

        val opts = DeepLinkOptions(skuAndroid = "skuX", packageNameAndroid = "dev.hyo.martie")
        store.deepLinkToSubscriptions(opts)
        assertEquals("skuX", fake.lastDeepLinkOptions?.skuAndroid)
        assertEquals("dev.hyo.martie", fake.lastDeepLinkOptions?.packageNameAndroid)
    }

    @Test
    fun getActiveSubscriptions_passThrough() = kotlinx.coroutines.test.runTest {
        val fake = FakeModule()
        val store = OpenIapStore(fake)
        val sub = OpenIapActiveSubscription(
            productId = "sku.sub",
            isActive = true,
            transactionId = "t",
            purchaseToken = "pt",
            transactionDate = System.currentTimeMillis(),
            platform = "android",
            autoRenewingAndroid = true
        )
        fake.activeSubsToReturn = listOf(sub)

        val result = store.getActiveSubscriptions(listOf("sku.sub"))
        assertEquals(1, result.size)
        assertEquals("sku.sub", result.first().productId)
    }
}
