package dev.hyo.openiap.models

/**
 * Error codes based on openiap.dev ErrorCode enum
 */
enum class OpenIapErrorCode(val value: String) {
    E_UNKNOWN("E_UNKNOWN"),
    E_USER_CANCELLED("E_USER_CANCELLED"),
    E_USER_ERROR("E_USER_ERROR"),
    E_ITEM_UNAVAILABLE("E_ITEM_UNAVAILABLE"),
    E_REMOTE_ERROR("E_REMOTE_ERROR"),
    E_NETWORK_ERROR("E_NETWORK_ERROR"),
    E_SERVICE_ERROR("E_SERVICE_ERROR"),
    E_RECEIPT_FAILED("E_RECEIPT_FAILED"),
    E_RECEIPT_FINISHED_FAILED("E_RECEIPT_FINISHED_FAILED"),
    E_NOT_PREPARED("E_NOT_PREPARED"),
    E_NOT_ENDED("E_NOT_ENDED"),
    E_ALREADY_OWNED("E_ALREADY_OWNED"),
    E_DEVELOPER_ERROR("E_DEVELOPER_ERROR"),
    E_BILLING_RESPONSE_JSON_PARSE_ERROR("E_BILLING_RESPONSE_JSON_PARSE_ERROR"),
    E_DEFERRED_PAYMENT("E_DEFERRED_PAYMENT"),
    E_INTERRUPTED("E_INTERRUPTED"),
    E_IAP_NOT_AVAILABLE("E_IAP_NOT_AVAILABLE"),
    E_PURCHASE_ERROR("E_PURCHASE_ERROR"),
    E_SYNC_ERROR("E_SYNC_ERROR"),
    E_TRANSACTION_VALIDATION_FAILED("E_TRANSACTION_VALIDATION_FAILED"),
    E_ACTIVITY_UNAVAILABLE("E_ACTIVITY_UNAVAILABLE"),
    E_ALREADY_PREPARED("E_ALREADY_PREPARED"),
    E_PENDING("E_PENDING"),
    E_CONNECTION_CLOSED("E_CONNECTION_CLOSED"),
    E_INIT_CONNECTION("E_INIT_CONNECTION"),
    E_SERVICE_DISCONNECTED("E_SERVICE_DISCONNECTED"),
    E_QUERY_PRODUCT("E_QUERY_PRODUCT"),
    E_SKU_NOT_FOUND("E_SKU_NOT_FOUND"),
    E_SKU_OFFER_MISMATCH("E_SKU_OFFER_MISMATCH"),
    E_ITEM_NOT_OWNED("E_ITEM_NOT_OWNED"),
    E_BILLING_UNAVAILABLE("E_BILLING_UNAVAILABLE"),
    E_FEATURE_NOT_SUPPORTED("E_FEATURE_NOT_SUPPORTED"),
    E_EMPTY_SKU_LIST("E_EMPTY_SKU_LIST"),
    E_SERVICE_UNAVAILABLE("E_SERVICE_UNAVAILABLE");
    
    companion object {
        fun fromBillingResponseCode(responseCode: Int): OpenIapErrorCode = when (responseCode) {
            -2 -> E_FEATURE_NOT_SUPPORTED
            -1 -> E_SERVICE_DISCONNECTED
            0 -> E_UNKNOWN // OK
            1 -> E_USER_CANCELLED
            2 -> E_SERVICE_UNAVAILABLE
            3 -> E_BILLING_UNAVAILABLE
            4 -> E_ITEM_UNAVAILABLE
            5 -> E_DEVELOPER_ERROR
            6 -> E_PURCHASE_ERROR
            7 -> E_ALREADY_OWNED
            8 -> E_ITEM_NOT_OWNED
            else -> E_UNKNOWN
        }
    }
}

/**
 * Purchase error type
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