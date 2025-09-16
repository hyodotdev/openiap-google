package dev.hyo.openiap.models

/**
 * Android receipt validation result
 * Based on openiap.dev ReceiptValidationResultAndroid
 */
data class ReceiptValidationResultAndroid(
    val autoRenewing: Boolean,
    val betaProduct: Boolean,
    val cancelDate: Long?,
    val cancelReason: String,
    val deferredDate: Long?,
    val deferredSku: Int?,
    val freeTrialEndDate: Long,
    val gracePeriodEndDate: Long,
    val parentProductId: String,
    val productId: String,
    val productType: String, // "inapp" or "subs"
    val purchaseDate: Long,
    val quantity: Int,
    val receiptId: String,
    val renewalDate: Long,
    val term: String,
    val termSku: String,
    val testTransaction: Boolean
)

/**
 * Validation options for receipt validation
 */
data class ReceiptValidationProps(
    val sku: String,
    val androidOptions: ReceiptValidationAndroidOptions? = null
) {
    data class ReceiptValidationAndroidOptions(
        val packageName: String,
        val productToken: String,
        val accessToken: String,
        val isSub: Boolean = false
    )
}

