package dev.hyo.openiap.models

/**
 * Android-specific purchase request parameters
 * Based on openiap.dev RequestPurchaseParams
 */
data class RequestPurchaseParams(
    val skus: List<String>,
    val obfuscatedAccountIdAndroid: String? = null,
    val obfuscatedProfileIdAndroid: String? = null,
    val isOfferPersonalized: Boolean? = null
)

/**
 * Android-specific subscription request parameters
 * Based on openiap.dev RequestSubscriptionAndroidProps
 */
data class RequestSubscriptionAndroidProps(
    val skus: List<String>,
    val obfuscatedAccountIdAndroid: String? = null,
    val obfuscatedProfileIdAndroid: String? = null,
    val isOfferPersonalized: Boolean? = null,
    val purchaseTokenAndroid: String? = null,
    val replacementModeAndroid: Int? = null,
    val subscriptionOffers: List<SubscriptionOffer>
) {
    data class SubscriptionOffer(
        val sku: String,
        val offerToken: String
    )
}

/**
 * Parameters for finishTransaction method
 */
data class FinishTransactionParams(
    val purchase: OpenIapPurchase,
    val isConsumable: Boolean = false
)

/**
 * Options for getAvailablePurchases method
 */
data class PurchaseOptions(
    // Android doesn't use these iOS-specific options, but keeping for interface compatibility
    val alsoPublishToEventListenerIOS: Boolean? = null,
    val onlyIncludeActiveItemsIOS: Boolean? = null
)

/**
 * Backwards-compatible ProductRequest (used throughout the module)
 */
data class ProductRequest(
    val skus: List<String>,
    val type: ProductRequestType = ProductRequestType.INAPP
) {
    enum class ProductRequestType(val value: String) {
        INAPP("inapp"),
        SUBS("subs"),
        ALL("all");

        companion object {
            fun fromString(value: String): ProductRequestType =
                values().find { it.value == value } ?: INAPP
        }
    }
}

/**
 * Options for deep-linking to subscription management (Android)
 * Matches openiap.dev API shape
 */
data class DeepLinkOptions(
    val skuAndroid: String? = null,
    val packageNameAndroid: String? = null
)
