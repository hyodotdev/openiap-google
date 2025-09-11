package dev.hyo.openiap.models

/**
 * Base product information for Android
 * Based on openiap.dev ProductAndroid type specification
 */
data class OpenIapProduct(
    // Common fields (shared with iOS)
    val id: String,
    val title: String,
    val description: String,
    val type: ProductType,
    val displayName: String? = null,
    val displayPrice: String,
    val currency: String,
    val price: Double? = null,
    val debugDescription: String? = null,
    val platform: String = "android",
    
    // Android-specific fields
    val nameAndroid: String,
    val oneTimePurchaseOfferDetailsAndroid: OneTimePurchaseOfferDetail? = null,
    val subscriptionOfferDetailsAndroid: List<SubscriptionOfferDetail>? = null
) {
    enum class ProductType(val value: String) {
        INAPP("inapp"),
        SUBS("subs");
        
        companion object {
            fun fromString(value: String): ProductType = 
                values().find { it.value == value } ?: INAPP
        }
    }

    fun toJSON(): Map<String, Any?> = mapOf(
        // Common fields
        "id" to id,
        "title" to title,
        "description" to description,
        "type" to type.value,
        "platform" to "android",

        // Display / pricing
        "displayName" to displayName,
        "displayPrice" to displayPrice,
        "currency" to currency,
        "price" to price,
        "debugDescription" to debugDescription,

        // Android-specific
        "nameAndroid" to nameAndroid,
        "oneTimePurchaseOfferDetailsAndroid" to oneTimePurchaseOfferDetailsAndroid?.let {
            mapOf(
                "priceCurrencyCode" to it.priceCurrencyCode,
                "formattedPrice" to it.formattedPrice,
                "priceAmountMicros" to it.priceAmountMicros,
            )
        },
        "subscriptionOfferDetailsAndroid" to subscriptionOfferDetailsAndroid?.map { offer ->
            mapOf(
                "basePlanId" to offer.basePlanId,
                "offerId" to offer.offerId,
                "offerToken" to offer.offerToken,
                "offerTags" to offer.offerTags,
                "pricingPhases" to mapOf(
                    "pricingPhaseList" to offer.pricingPhases.pricingPhaseList.map { phase ->
                        mapOf(
                            "formattedPrice" to phase.formattedPrice,
                            "priceCurrencyCode" to phase.priceCurrencyCode,
                            "billingPeriod" to phase.billingPeriod,
                            "billingCycleCount" to phase.billingCycleCount,
                            "priceAmountMicros" to phase.priceAmountMicros,
                            "recurrenceMode" to phase.recurrenceMode,
                        )
                    },
                ),
            )
        },
    )
}

/**
 * Android one-time purchase offer details
 */
data class OneTimePurchaseOfferDetail(
    val priceCurrencyCode: String,
    val formattedPrice: String,
    val priceAmountMicros: String
)

/**
 * Android subscription offer details
 */
data class SubscriptionOfferDetail(
    val basePlanId: String,
    val offerId: String?,
    val offerToken: String,
    val offerTags: List<String>,
    val pricingPhases: PricingPhases
)

/**
 * Android pricing phases for subscriptions
 */
data class PricingPhases(
    val pricingPhaseList: List<PricingPhase>
)

/**
 * Android pricing phase details
 */
data class PricingPhase(
    val formattedPrice: String,
    val priceCurrencyCode: String,
    val billingPeriod: String, // P1W, P1M, P1Y
    val billingCycleCount: Int,
    val priceAmountMicros: String,
    val recurrenceMode: Int
)
