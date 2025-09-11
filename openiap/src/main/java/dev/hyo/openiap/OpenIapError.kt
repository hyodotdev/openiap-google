package dev.hyo.openiap

import com.android.billingclient.api.BillingClient

/**
 * OpenIAP specific exceptions
 */
sealed class OpenIapError : Exception() {
    abstract val code: String
    abstract override val message: String

    data class ProductNotFound(val productId: String) : OpenIapError() {
        override val code = "PRODUCT_NOT_FOUND"
        override val message = "Product not found: $productId"
    }

    data class PurchaseFailed(override val message: String) : OpenIapError() {
        override val code = "PURCHASE_FAILED"
    }

    object PurchaseCancelled : OpenIapError() {
        override val code = "PURCHASE_CANCELLED"
        override val message = "Purchase was cancelled by the user"
    }

    object PurchaseDeferred : OpenIapError() {
        override val code = "PURCHASE_DEFERRED"
        override val message = "Purchase was deferred"
    }

    object PaymentNotAllowed : OpenIapError() {
        override val code = "PAYMENT_NOT_ALLOWED"
        override val message = "Payment not allowed"
    }

    data class BillingError(override val message: String) : OpenIapError() {
        override val code = "BILLING_ERROR"
    }

    data class InvalidReceipt(override val message: String) : OpenIapError() {
        override val code = "INVALID_RECEIPT"
    }

    object NetworkError : OpenIapError() {
        override val code = "NETWORK_ERROR"
        override val message = "Network connection error"
    }

    data class VerificationFailed(override val message: String) : OpenIapError() {
        override val code = "VERIFICATION_FAILED"
    }

    data class RestoreFailed(override val message: String) : OpenIapError() {
        override val code = "RESTORE_FAILED"
    }

    data class UnknownError(override val message: String) : OpenIapError() {
        override val code = "UNKNOWN_ERROR"
    }

    object NotSupported : OpenIapError() {
        override val code = "NOT_SUPPORTED"
        override val message = "Operation not supported"
    }

    object NotPrepared : OpenIapError() {
        override val code = "NOT_PREPARED"
        override val message = "Billing client not ready"
    }

    data class InitConnection(override val message: String) : OpenIapError() {
        override val code = "INIT_CONNECTION"
    }

    data class QueryProduct(override val message: String) : OpenIapError() {
        override val code = "QUERY_PRODUCT"
    }

    object EmptySkuList : OpenIapError() {
        override val code = "EMPTY_SKU_LIST"
        override val message = "SKU list cannot be empty"
    }

    data class SkuNotFound(val sku: String) : OpenIapError() {
        override val code = "SKU_NOT_FOUND"
        override val message = "SKU not found in cached products: $sku"
    }

    object SkuOfferMismatch : OpenIapError() {
        override val code = "SKU_OFFER_MISMATCH"
        override val message = "SKU and offer token count mismatch"
    }

    object MissingCurrentActivity : OpenIapError() {
        override val code = "MISSING_CURRENT_ACTIVITY"
        override val message = "Current activity is not available"
    }

    object UserCancelled : OpenIapError() {
        override val code = "USER_CANCELLED"
        override val message = "User cancelled the operation"
    }

    object ItemAlreadyOwned : OpenIapError() {
        override val code = "ITEM_ALREADY_OWNED"
        override val message = "Item is already owned"
    }

    object ItemNotOwned : OpenIapError() {
        override val code = "ITEM_NOT_OWNED"
        override val message = "Item is not owned"
    }

    object ServiceUnavailable : OpenIapError() {
        override val code = "SERVICE_UNAVAILABLE"
        override val message = "Google Play service is unavailable"
    }

    object BillingUnavailable : OpenIapError() {
        override val code = "BILLING_UNAVAILABLE"
        override val message = "Billing API version is not supported"
    }

    object ItemUnavailable : OpenIapError() {
        override val code = "ITEM_UNAVAILABLE"
        override val message = "Requested product is not available for purchase"
    }

    object DeveloperError : OpenIapError() {
        override val code = "DEVELOPER_ERROR"
        override val message = "Invalid arguments provided to the API"
    }

    object FeatureNotSupported : OpenIapError() {
        override val code = "FEATURE_NOT_SUPPORTED"
        override val message = "Requested feature is not supported by Play Store"
    }

    object ServiceDisconnected : OpenIapError() {
        override val code = "SERVICE_DISCONNECTED"
        override val message = "Play Store service is not connected"
    }

    object ServiceTimeout : OpenIapError() {
        override val code = "SERVICE_TIMEOUT"
        override val message = "The request has reached the maximum timeout before Google Play responds"
    }

    companion object {
        // ---------------------------------------------------------------------
        // OpenIAP error code constants (parity with openiap-apple)
        // ---------------------------------------------------------------------
        const val E_UNKNOWN = "E_UNKNOWN"
        const val E_USER_CANCELLED = "E_USER_CANCELLED"
        const val E_USER_ERROR = "E_USER_ERROR"
        const val E_ITEM_UNAVAILABLE = "E_ITEM_UNAVAILABLE"
        const val E_REMOTE_ERROR = "E_REMOTE_ERROR"
        const val E_NETWORK_ERROR = "E_NETWORK_ERROR"
        const val E_SERVICE_ERROR = "E_SERVICE_ERROR"
        const val E_RECEIPT_FAILED = "E_RECEIPT_FAILED"
        const val E_RECEIPT_FINISHED_FAILED = "E_RECEIPT_FINISHED_FAILED"
        const val E_NOT_PREPARED = "E_NOT_PREPARED"
        const val E_NOT_ENDED = "E_NOT_ENDED"
        const val E_ALREADY_OWNED = "E_ALREADY_OWNED"
        const val E_DEVELOPER_ERROR = "E_DEVELOPER_ERROR"
        const val E_BILLING_RESPONSE_JSON_PARSE_ERROR = "E_BILLING_RESPONSE_JSON_PARSE_ERROR"
        const val E_DEFERRED_PAYMENT = "E_DEFERRED_PAYMENT"
        const val E_INTERRUPTED = "E_INTERRUPTED"
        const val E_IAP_NOT_AVAILABLE = "E_IAP_NOT_AVAILABLE"
        const val E_PURCHASE_ERROR = "E_PURCHASE_ERROR"
        const val E_SYNC_ERROR = "E_SYNC_ERROR"
        const val E_TRANSACTION_VALIDATION_FAILED = "E_TRANSACTION_VALIDATION_FAILED"
        const val E_ACTIVITY_UNAVAILABLE = "E_ACTIVITY_UNAVAILABLE"
        const val E_ALREADY_PREPARED = "E_ALREADY_PREPARED"
        const val E_PENDING = "E_PENDING"
        const val E_CONNECTION_CLOSED = "E_CONNECTION_CLOSED"
        const val E_INIT_CONNECTION = "E_INIT_CONNECTION"
        const val E_SERVICE_DISCONNECTED = "E_SERVICE_DISCONNECTED"
        const val E_QUERY_PRODUCT = "E_QUERY_PRODUCT"
        const val E_SKU_NOT_FOUND = "E_SKU_NOT_FOUND"
        const val E_SKU_OFFER_MISMATCH = "E_SKU_OFFER_MISMATCH"
        const val E_ITEM_NOT_OWNED = "E_ITEM_NOT_OWNED"
        const val E_BILLING_UNAVAILABLE = "E_BILLING_UNAVAILABLE"
        const val E_FEATURE_NOT_SUPPORTED = "E_FEATURE_NOT_SUPPORTED"
        const val E_EMPTY_SKU_LIST = "E_EMPTY_SKU_LIST"
        const val E_SERVICE_UNAVAILABLE = "E_SERVICE_UNAVAILABLE"

        // ---------------------------------------------------------------------
        // Helpers: map between sealed class and E_* string codes
        // ---------------------------------------------------------------------
        fun toCode(error: OpenIapError): String = when (error) {
            is PurchaseCancelled -> E_USER_CANCELLED
            is UserCancelled -> E_USER_CANCELLED
            is PurchaseDeferred -> E_DEFERRED_PAYMENT
            is ServiceUnavailable -> E_SERVICE_UNAVAILABLE
            is BillingUnavailable -> E_BILLING_UNAVAILABLE
            is ItemUnavailable -> E_ITEM_UNAVAILABLE
            is DeveloperError -> E_DEVELOPER_ERROR
            is ItemAlreadyOwned -> E_ALREADY_OWNED
            is ItemNotOwned -> E_ITEM_NOT_OWNED
            is ServiceDisconnected -> E_SERVICE_DISCONNECTED
            is FeatureNotSupported -> E_FEATURE_NOT_SUPPORTED
            is ServiceTimeout -> E_SERVICE_ERROR
            is NetworkError -> E_NETWORK_ERROR
            is BillingError -> E_SERVICE_ERROR
            is NotPrepared -> E_NOT_PREPARED
            is InitConnection -> E_INIT_CONNECTION
            is QueryProduct -> E_QUERY_PRODUCT
            is EmptySkuList -> E_EMPTY_SKU_LIST
            is SkuNotFound -> E_SKU_NOT_FOUND
            is SkuOfferMismatch -> E_SKU_OFFER_MISMATCH
            is MissingCurrentActivity -> E_ACTIVITY_UNAVAILABLE
            is PaymentNotAllowed -> E_IAP_NOT_AVAILABLE
            is RestoreFailed -> E_SERVICE_ERROR
            is VerificationFailed -> E_TRANSACTION_VALIDATION_FAILED
            is InvalidReceipt -> E_RECEIPT_FAILED
            is PurchaseFailed -> E_PURCHASE_ERROR
            is ProductNotFound -> E_SKU_NOT_FOUND
            is UnknownError -> E_UNKNOWN
            is NotSupported -> E_FEATURE_NOT_SUPPORTED
        }

        fun defaultMessage(code: String): String = when (code) {
            E_USER_CANCELLED -> "User cancelled the purchase flow"
            E_USER_ERROR -> "User action error"
            E_DEFERRED_PAYMENT -> "Payment was deferred"
            E_INTERRUPTED -> "Purchase flow interrupted"
            E_ITEM_UNAVAILABLE -> "Item unavailable"
            E_SKU_NOT_FOUND -> "SKU not found"
            E_SKU_OFFER_MISMATCH -> "SKU offer mismatch"
            E_QUERY_PRODUCT -> "Failed to query product"
            E_ALREADY_OWNED -> "Item already owned"
            E_ITEM_NOT_OWNED -> "Item not owned"
            E_NETWORK_ERROR -> "Network connection error"
            E_SERVICE_ERROR -> "Store service error"
            E_REMOTE_ERROR -> "Remote service error"
            E_INIT_CONNECTION -> "Failed to initialize billing connection"
            E_SERVICE_DISCONNECTED -> "Billing service disconnected"
            E_CONNECTION_CLOSED -> "Connection closed"
            E_IAP_NOT_AVAILABLE -> "In-app purchases not available on this device"
            E_BILLING_UNAVAILABLE -> "Billing unavailable"
            E_FEATURE_NOT_SUPPORTED -> "Feature not supported on this platform"
            E_SYNC_ERROR -> "Sync error"
            E_RECEIPT_FAILED -> "Receipt validation failed"
            E_RECEIPT_FINISHED_FAILED -> "Receipt finish failed"
            E_TRANSACTION_VALIDATION_FAILED -> "Transaction validation failed"
            E_EMPTY_SKU_LIST -> "Empty SKU list provided"
            E_NOT_PREPARED -> "Billing is not prepared"
            E_NOT_ENDED -> "Billing connection not ended"
            E_DEVELOPER_ERROR -> "Developer configuration error"
            E_PURCHASE_ERROR -> "Purchase error"
            E_ACTIVITY_UNAVAILABLE -> "Required activity is unavailable"
            E_ALREADY_PREPARED -> "Billing already prepared"
            E_PENDING -> "Transaction pending"
            E_BILLING_RESPONSE_JSON_PARSE_ERROR -> "Billing response parse error"
            E_SERVICE_UNAVAILABLE -> "Service unavailable"
            else -> "Unknown error occurred"
        }
        /**
         * Convert Google Play Billing response code to OpenIapError
         */
        fun fromBillingResponseCode(responseCode: Int, debugMessage: String? = null): OpenIapError {
            return when (responseCode) {
                BillingClient.BillingResponseCode.USER_CANCELED -> UserCancelled
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> ServiceUnavailable
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> BillingUnavailable
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> ItemUnavailable
                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> DeveloperError
                BillingClient.BillingResponseCode.ERROR -> BillingError(debugMessage ?: "Billing error")
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> ItemAlreadyOwned
                BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> ItemNotOwned
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> ServiceDisconnected
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> FeatureNotSupported
                BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> ServiceTimeout
                else -> UnknownError(debugMessage ?: "Unknown error: $responseCode")
            }
        }

        /**
         * Get all error codes as a map for external use
         */
        fun getAllErrorCodes(): Map<String, String> = mapOf(
            // User Action Errors
            E_USER_CANCELLED to defaultMessage(E_USER_CANCELLED),
            E_USER_ERROR to defaultMessage(E_USER_ERROR),
            E_DEFERRED_PAYMENT to defaultMessage(E_DEFERRED_PAYMENT),
            E_INTERRUPTED to defaultMessage(E_INTERRUPTED),

            // Product Errors
            E_ITEM_UNAVAILABLE to defaultMessage(E_ITEM_UNAVAILABLE),
            E_SKU_NOT_FOUND to defaultMessage(E_SKU_NOT_FOUND),
            E_SKU_OFFER_MISMATCH to defaultMessage(E_SKU_OFFER_MISMATCH),
            E_QUERY_PRODUCT to defaultMessage(E_QUERY_PRODUCT),
            E_ALREADY_OWNED to defaultMessage(E_ALREADY_OWNED),
            E_ITEM_NOT_OWNED to defaultMessage(E_ITEM_NOT_OWNED),

            // Network & Service Errors
            E_NETWORK_ERROR to defaultMessage(E_NETWORK_ERROR),
            E_SERVICE_ERROR to defaultMessage(E_SERVICE_ERROR),
            E_REMOTE_ERROR to defaultMessage(E_REMOTE_ERROR),
            E_INIT_CONNECTION to defaultMessage(E_INIT_CONNECTION),
            E_SERVICE_DISCONNECTED to defaultMessage(E_SERVICE_DISCONNECTED),
            E_CONNECTION_CLOSED to defaultMessage(E_CONNECTION_CLOSED),
            E_IAP_NOT_AVAILABLE to defaultMessage(E_IAP_NOT_AVAILABLE),
            E_BILLING_UNAVAILABLE to defaultMessage(E_BILLING_UNAVAILABLE),
            E_FEATURE_NOT_SUPPORTED to defaultMessage(E_FEATURE_NOT_SUPPORTED),
            E_SYNC_ERROR to defaultMessage(E_SYNC_ERROR),

            // Validation Errors
            E_RECEIPT_FAILED to defaultMessage(E_RECEIPT_FAILED),
            E_RECEIPT_FINISHED_FAILED to defaultMessage(E_RECEIPT_FINISHED_FAILED),
            E_TRANSACTION_VALIDATION_FAILED to defaultMessage(E_TRANSACTION_VALIDATION_FAILED),
            E_EMPTY_SKU_LIST to defaultMessage(E_EMPTY_SKU_LIST),

            // Platform/Parsing
            E_BILLING_RESPONSE_JSON_PARSE_ERROR to defaultMessage(E_BILLING_RESPONSE_JSON_PARSE_ERROR),
            E_ACTIVITY_UNAVAILABLE to defaultMessage(E_ACTIVITY_UNAVAILABLE),

            // Lifecycle/Generic
            E_NOT_PREPARED to defaultMessage(E_NOT_PREPARED),
            E_NOT_ENDED to defaultMessage(E_NOT_ENDED),
            E_ALREADY_PREPARED to defaultMessage(E_ALREADY_PREPARED),
            E_PENDING to defaultMessage(E_PENDING),
            E_PURCHASE_ERROR to defaultMessage(E_PURCHASE_ERROR),
            E_SERVICE_UNAVAILABLE to defaultMessage(E_SERVICE_UNAVAILABLE),

            // Generic
            E_UNKNOWN to defaultMessage(E_UNKNOWN)
        )
    }
}
