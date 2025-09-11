package dev.hyo.openiap.models

/**
 * Base purchase information for Android
 * Based on openiap.dev PurchaseAndroid type specification
 */
data class OpenIapPurchase(
    // Common fields (shared with iOS)
    val id: String,
    val productId: String,
    val ids: List<String>? = null,
    val transactionId: String? = null, // deprecated - use id instead
    val transactionDate: Long,
    val transactionReceipt: String,
    val purchaseToken: String? = null, // unified purchase token
    val platform: String = "android",
    val quantity: Int = 1,
    val purchaseState: PurchaseState,
    val isAutoRenewing: Boolean,
    
    // Android-specific fields
    val purchaseTokenAndroid: String? = null, // deprecated - use purchaseToken instead
    val dataAndroid: String? = null,
    val signatureAndroid: String? = null,
    val autoRenewingAndroid: Boolean? = null,
    val isAcknowledgedAndroid: Boolean? = null,
    val packageNameAndroid: String? = null,
    val developerPayloadAndroid: String? = null,
    val obfuscatedAccountIdAndroid: String? = null,
    val obfuscatedProfileIdAndroid: String? = null
) {
    enum class PurchaseState(val value: String) {
        PENDING("pending"),
        PURCHASED("purchased"),
        FAILED("failed"),
        RESTORED("restored"), // iOS only but keeping for compatibility
        DEFERRED("deferred"), // iOS only but keeping for compatibility
        UNKNOWN("unknown");
        
        companion object {
            fun fromString(value: String): PurchaseState = 
                values().find { it.value == value } ?: UNKNOWN
                
            fun fromBillingClientState(state: Int): PurchaseState = when (state) {
                0 -> UNKNOWN // UNSPECIFIED_STATE
                1 -> PURCHASED
                2 -> PENDING
                else -> UNKNOWN
            }
        }
    }

    fun toJSON(): Map<String, Any?> = mapOf(
        // Common
        "id" to id,
        "productId" to productId,
        "ids" to ids,
        "transactionId" to transactionId,
        "transactionDate" to transactionDate.toDouble(),
        "transactionReceipt" to transactionReceipt,
        "purchaseToken" to purchaseToken,
        "platform" to "android",

        // Android-specific
        "purchaseTokenAndroid" to purchaseTokenAndroid,
        "dataAndroid" to dataAndroid,
        "signatureAndroid" to signatureAndroid,
        "autoRenewingAndroid" to autoRenewingAndroid,
        "isAcknowledgedAndroid" to isAcknowledgedAndroid,
        "packageNameAndroid" to packageNameAndroid,
        "developerPayloadAndroid" to developerPayloadAndroid,
    )
}
