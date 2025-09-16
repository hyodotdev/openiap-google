package dev.hyo.openiap

import com.android.billingclient.api.BillingClient

/**
 * OpenIAP specific exceptions
 */
sealed class OpenIapError : Exception() {
    abstract val code: String
    abstract override val message: String

    fun toJSON(): Map<String, Any?> = mapOf(
        "code" to toCode(this),
        "message" to (this.message ?: ""),
        "platform" to "android",
    )

    class ProductNotFound(val productId: String) : OpenIapError() {
        override val code = CODE
        override val message = MESSAGE

        companion object {
            const val CODE = "PRODUCT_NOT_FOUND"
            const val MESSAGE = "Product not found"
        }
    }

    class PurchaseFailed : OpenIapError() {
        override val code = CODE
        override val message = MESSAGE

        companion object {
            const val CODE = "PURCHASE_FAILED"
            const val MESSAGE = "Purchase failed"
        }
    }

    object PurchaseCancelled : OpenIapError() {
        const val CODE = "PURCHASE_CANCELLED"
        const val MESSAGE = "Purchase was cancelled by the user"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    object PurchaseDeferred : OpenIapError() {
        const val CODE = "PURCHASE_DEFERRED"
        const val MESSAGE = "Purchase was deferred"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    object PaymentNotAllowed : OpenIapError() {
        const val CODE = "PAYMENT_NOT_ALLOWED"
        const val MESSAGE = "Payment not allowed"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    class BillingError : OpenIapError() {
        override val code = CODE
        override val message = MESSAGE

        companion object {
            const val CODE = "BILLING_ERROR"
            const val MESSAGE = "Billing error"
        }
    }

    class InvalidReceipt : OpenIapError() {
        override val code = CODE
        override val message = MESSAGE

        companion object {
            const val CODE = "INVALID_RECEIPT"
            const val MESSAGE = "Invalid receipt"
        }
    }

    object NetworkError : OpenIapError() {
        const val CODE = "NETWORK_ERROR"
        const val MESSAGE = "Network connection error"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    class VerificationFailed : OpenIapError() {
        override val code = CODE
        override val message = MESSAGE

        companion object {
            const val CODE = "VERIFICATION_FAILED"
            const val MESSAGE = "Verification failed"
        }
    }

    class RestoreFailed : OpenIapError() {
        override val code = CODE
        override val message = MESSAGE

        companion object {
            const val CODE = "RESTORE_FAILED"
            const val MESSAGE = "Restore failed"
        }
    }

    class UnknownError : OpenIapError() {
        override val code = CODE
        override val message = MESSAGE

        companion object {
            const val CODE = "UNKNOWN_ERROR"
            const val MESSAGE = "Unknown error"
        }
    }

    object NotSupported : OpenIapError() {
        const val CODE = "NOT_SUPPORTED"
        const val MESSAGE = "Operation not supported"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    object NotPrepared : OpenIapError() {
        const val CODE = "NOT_PREPARED"
        const val MESSAGE = "Billing client not ready"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    class InitConnection : OpenIapError() {
        override val code = CODE
        override val message = MESSAGE

        companion object {
            const val CODE = "INIT_CONNECTION"
            const val MESSAGE = "Failed to initialize billing connection"
        }
    }

    class QueryProduct : OpenIapError() {
        override val code = CODE
        override val message = MESSAGE

        companion object {
            const val CODE = "QUERY_PRODUCT"
            const val MESSAGE = "Failed to query product"
        }
    }

    object EmptySkuList : OpenIapError() {
        const val CODE = "EMPTY_SKU_LIST"
        const val MESSAGE = "SKU list cannot be empty"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    class SkuNotFound(val sku: String) : OpenIapError() {
        override val code = CODE
        override val message = MESSAGE

        companion object {
            const val CODE = "SKU_NOT_FOUND"
            const val MESSAGE = "SKU not found"
        }
    }

    object SkuOfferMismatch : OpenIapError() {
        const val CODE = "SKU_OFFER_MISMATCH"
        const val MESSAGE = "SKU and offer token count mismatch"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    object MissingCurrentActivity : OpenIapError() {
        const val CODE = "MISSING_CURRENT_ACTIVITY"
        const val MESSAGE = "Current activity is not available"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    object UserCancelled : OpenIapError() {
        const val CODE = "USER_CANCELLED"
        const val MESSAGE = "User cancelled the operation"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    object ItemAlreadyOwned : OpenIapError() {
        const val CODE = "ITEM_ALREADY_OWNED"
        const val MESSAGE = "Item is already owned"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    object ItemNotOwned : OpenIapError() {
        const val CODE = "ITEM_NOT_OWNED"
        const val MESSAGE = "Item is not owned"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    object ServiceUnavailable : OpenIapError() {
        const val CODE = "SERVICE_UNAVAILABLE"
        const val MESSAGE = "Google Play service is unavailable"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    object BillingUnavailable : OpenIapError() {
        const val CODE = "BILLING_UNAVAILABLE"
        const val MESSAGE = "Billing API version is not supported"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    object ItemUnavailable : OpenIapError() {
        const val CODE = "ITEM_UNAVAILABLE"
        const val MESSAGE = "Requested product is not available for purchase"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    object DeveloperError : OpenIapError() {
        const val CODE = "DEVELOPER_ERROR"
        const val MESSAGE = "Invalid arguments provided to the API"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    object FeatureNotSupported : OpenIapError() {
        const val CODE = "FEATURE_NOT_SUPPORTED"
        const val MESSAGE = "Requested feature is not supported by Play Store"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    object ServiceDisconnected : OpenIapError() {
        const val CODE = "SERVICE_DISCONNECTED"
        const val MESSAGE = "Play Store service is not connected"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    object ServiceTimeout : OpenIapError() {
        const val CODE = "SERVICE_TIMEOUT"
        const val MESSAGE = "The request has reached the maximum timeout before Google Play responds"
        override val code: String = CODE
        override val message: String = MESSAGE
    }

    companion object {
        private val defaultMessages: Map<String, String> by lazy {
            mapOf(
                ProductNotFound.CODE to ProductNotFound.MESSAGE,
                PurchaseFailed.CODE to PurchaseFailed.MESSAGE,
                PurchaseCancelled.CODE to PurchaseCancelled.MESSAGE,
                PurchaseDeferred.CODE to PurchaseDeferred.MESSAGE,
                PaymentNotAllowed.CODE to PaymentNotAllowed.MESSAGE,
                BillingError.CODE to BillingError.MESSAGE,
                InvalidReceipt.CODE to InvalidReceipt.MESSAGE,
                NetworkError.CODE to NetworkError.MESSAGE,
                VerificationFailed.CODE to VerificationFailed.MESSAGE,
                RestoreFailed.CODE to RestoreFailed.MESSAGE,
                UnknownError.CODE to UnknownError.MESSAGE,
                NotSupported.CODE to NotSupported.MESSAGE,
                NotPrepared.CODE to NotPrepared.MESSAGE,
                InitConnection.CODE to InitConnection.MESSAGE,
                QueryProduct.CODE to QueryProduct.MESSAGE,
                EmptySkuList.CODE to EmptySkuList.MESSAGE,
                SkuNotFound.CODE to SkuNotFound.MESSAGE,
                SkuOfferMismatch.CODE to SkuOfferMismatch.MESSAGE,
                MissingCurrentActivity.CODE to MissingCurrentActivity.MESSAGE,
                UserCancelled.CODE to UserCancelled.MESSAGE,
                ItemAlreadyOwned.CODE to ItemAlreadyOwned.MESSAGE,
                ItemNotOwned.CODE to ItemNotOwned.MESSAGE,
                ServiceUnavailable.CODE to ServiceUnavailable.MESSAGE,
                BillingUnavailable.CODE to BillingUnavailable.MESSAGE,
                ItemUnavailable.CODE to ItemUnavailable.MESSAGE,
                DeveloperError.CODE to DeveloperError.MESSAGE,
                FeatureNotSupported.CODE to FeatureNotSupported.MESSAGE,
                ServiceDisconnected.CODE to ServiceDisconnected.MESSAGE,
                ServiceTimeout.CODE to ServiceTimeout.MESSAGE
            )
        }

        fun toCode(error: OpenIapError): String = error.code

        fun defaultMessage(code: String): String =
            defaultMessages[code] ?: "Unknown error occurred"

        @Suppress("DEPRECATION")
        fun fromBillingResponseCode(responseCode: Int, debugMessage: String? = null): OpenIapError {
            return when (responseCode) {
                BillingClient.BillingResponseCode.USER_CANCELED -> UserCancelled
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> ServiceUnavailable
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> BillingUnavailable
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> ItemUnavailable
                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> DeveloperError
                BillingClient.BillingResponseCode.ERROR -> BillingError()
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> ItemAlreadyOwned
                BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> ItemNotOwned
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> ServiceDisconnected
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> FeatureNotSupported
                BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> ServiceTimeout
                else -> UnknownError()
            }
        }

        fun getAllErrorCodes(): Map<String, String> = defaultMessages
    }

}
