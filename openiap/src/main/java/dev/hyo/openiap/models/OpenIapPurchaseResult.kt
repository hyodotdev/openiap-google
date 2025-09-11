package dev.hyo.openiap.models

/**
 * Purchase error type (kept minimal on Android)
 */
data class PurchaseError(
    val code: String,
    val message: String,
    val productId: String? = null
)

/**
 * Purchase result from finishTransaction
 */
data class PurchaseResult(
    val responseCode: Int? = null,
    val debugMessage: String? = null,
    val code: String? = null,
    val message: String? = null,
    val purchaseTokenAndroid: String? = null, // deprecated
    val purchaseToken: String? = null
)

