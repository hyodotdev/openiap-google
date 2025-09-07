package dev.hyo.openiap.helpers

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
        fun getAllErrorCodes(): Map<String, String> {
            return mapOf(
                "PRODUCT_NOT_FOUND" to "Product not found",
                "PURCHASE_FAILED" to "Purchase failed",
                "PURCHASE_CANCELLED" to "Purchase was cancelled by the user",
                "PURCHASE_DEFERRED" to "Purchase was deferred",
                "PAYMENT_NOT_ALLOWED" to "Payment not allowed",
                "BILLING_ERROR" to "Google Play Billing error",
                "INVALID_RECEIPT" to "Invalid receipt data",
                "NETWORK_ERROR" to "Network connection error",
                "VERIFICATION_FAILED" to "Transaction verification failed",
                "RESTORE_FAILED" to "Failed to restore purchases",
                "UNKNOWN_ERROR" to "An unknown error occurred",
                "NOT_SUPPORTED" to "Operation not supported",
                "NOT_PREPARED" to "Billing client not ready",
                "INIT_CONNECTION" to "Failed to initialize billing connection",
                "QUERY_PRODUCT" to "Failed to query product details",
                "EMPTY_SKU_LIST" to "SKU list cannot be empty",
                "SKU_NOT_FOUND" to "SKU not found in cached products",
                "SKU_OFFER_MISMATCH" to "SKU and offer token count mismatch",
                "MISSING_CURRENT_ACTIVITY" to "Current activity is not available",
                "USER_CANCELLED" to "User cancelled the operation",
                "ITEM_ALREADY_OWNED" to "Item is already owned",
                "ITEM_NOT_OWNED" to "Item is not owned",
                "SERVICE_UNAVAILABLE" to "Google Play service is unavailable",
                "BILLING_UNAVAILABLE" to "Billing API version is not supported",
                "ITEM_UNAVAILABLE" to "Requested product is not available for purchase",
                "DEVELOPER_ERROR" to "Invalid arguments provided to the API",
                "FEATURE_NOT_SUPPORTED" to "Requested feature is not supported by Play Store",
                "SERVICE_DISCONNECTED" to "Play Store service is not connected",
                "SERVICE_TIMEOUT" to "The request has reached the maximum timeout before Google Play responds"
            )
        }
    }
}
