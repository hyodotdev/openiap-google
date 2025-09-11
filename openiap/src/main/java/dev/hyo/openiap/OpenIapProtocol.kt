package dev.hyo.openiap

import dev.hyo.openiap.listener.OpenIapPurchaseErrorListener
import dev.hyo.openiap.listener.OpenIapPurchaseUpdateListener
import dev.hyo.openiap.models.*

/**
 * OpenIAP Protocol interface providing all in-app purchase functionality
 * Based on openiap.dev API specification for Android platform
 */
interface OpenIapProtocol {
    // ============================================================================
    // Connection Management
    // ============================================================================
    
    /**
     * Initialize connection to the store service.
     * Returns true if successful.
     */
    suspend fun initConnection(): Boolean
    
    /**
     * End connection to the store service.
     * Closes the connection and cleans up resources.
     */
    suspend fun endConnection(): Boolean

    // ============================================================================
    // Product Management
    // ============================================================================
    
    /**
     * Retrieve products or subscriptions from the store.
     * @param params ProductRequest containing SKUs and optional type filter
     * @return List of products matching the provided SKUs
     */
    suspend fun fetchProducts(params: ProductRequest): List<OpenIapProduct>
    
    /**
     * Get all available purchases for the current user.
     * This method handles restore functionality internally.
     * Returns:
     * - Consumables that haven't been consumed (finished with isConsumable=true)
     * - Non-consumables that haven't been finished  
     * - Active subscriptions
     * @param options Optional purchase options (Android-specific parameters ignored)
     * @return List of available purchases
     */
    suspend fun getAvailablePurchases(
        options: PurchaseOptions? = null
    ): List<OpenIapPurchase>

    /**
     * Get all active subscriptions with details.
     * If subscriptionIds is provided, filters to those SKUs; otherwise returns all.
     */
    suspend fun getActiveSubscriptions(
        subscriptionIds: List<String>? = null
    ): List<OpenIapActiveSubscription>

    /**
     * Check if there is at least one active subscription.
     */
    suspend fun hasActiveSubscriptions(
        subscriptionIds: List<String>? = null
    ): Boolean

    /**
     * Get available purchases filtered by type (inapp or subs).
     */
    suspend fun getAvailableItems(type: dev.hyo.openiap.models.ProductRequest.ProductRequestType): List<OpenIapPurchase>

    // ============================================================================
    // Purchase Operations
    // ============================================================================
    
    /**
     * Request a purchase for products or subscriptions.
     * @param request Purchase request parameters
     * @param type Product type ('inapp' | 'subs'), required for Android
     * @return Purchase object if successful, null if cancelled
     */
    suspend fun requestPurchase(
        request: RequestPurchaseAndroidProps,
        type: ProductRequest.ProductRequestType = ProductRequest.ProductRequestType.INAPP
    ): List<OpenIapPurchase>
    
    /**
     * Complete a purchase transaction.
     * Must be called after successful receipt validation for ALL purchase types.
     * @param params FinishTransactionParams containing purchase and consumable flag
     */
    suspend fun finishTransaction(
        params: FinishTransactionParams
    ): PurchaseResult

    // ============================================================================
    // Validation
    // ============================================================================
    
    /**
     * Validate a receipt with your server or platform servers.
     * All purchase types should be validated before granting entitlements.
     * @param sku Product SKU to validate
     * @param androidOptions Android-specific validation options
     * @return Validation result with receipt data
     */
    suspend fun validateReceipt(
        sku: String,
        androidOptions: ReceiptValidationProps.AndroidValidationOptions? = null
    ): ReceiptValidationResultAndroid?

    /**
     * Overload matching openiap.dev validateReceipt(options: ReceiptValidationProps)
     */
    suspend fun validateReceipt(options: ReceiptValidationProps): ReceiptValidationResultAndroid?

    // ============================================================================
    // Android-Specific APIs
    // ============================================================================
    
    /**
     * Acknowledge a non-consumable purchase or subscription.
     * Required within 3 days or the purchase will be refunded.
     * Note: This is called automatically by finishTransaction() when isConsumable is false.
     * @param purchaseToken Purchase token to acknowledge
     */
    suspend fun acknowledgePurchaseAndroid(purchaseToken: String)
    
    /**
     * Consume a purchase (for consumable products only).
     * Marks a consumable product as consumed, allowing repurchase.
     * Note: This is called automatically by finishTransaction() when isConsumable is true.
     * @param purchaseToken Purchase token to consume
     */
    suspend fun consumePurchaseAndroid(purchaseToken: String)

    // ============================================================================
    // Subscription Management / UX helpers
    // ============================================================================

    /**
     * Open native subscription management interface (Android deep link).
     */
    suspend fun deepLinkToSubscriptions(options: DeepLinkOptions)

    /**
     * Get storefront country code from Google Play Billing config.
     * Returns empty string on failure.
     */
    suspend fun getStorefront(): String

    // ============================================================================
    // Event Listeners (align with event-based request semantics)
    // ============================================================================

    fun addPurchaseUpdateListener(listener: OpenIapPurchaseUpdateListener)
    fun removePurchaseUpdateListener(listener: OpenIapPurchaseUpdateListener)
    fun addPurchaseErrorListener(listener: OpenIapPurchaseErrorListener)
    fun removePurchaseErrorListener(listener: OpenIapPurchaseErrorListener)
}
