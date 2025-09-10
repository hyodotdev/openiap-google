package dev.hyo.openiap.models

import com.google.gson.annotations.SerializedName

/**
 * Active subscription information per OpenIAP spec (Android variant)
 */
data class OpenIapActiveSubscription(
    @SerializedName("productId")
    val productId: String,

    @SerializedName("isActive")
    val isActive: Boolean,

    // Common fields for active subscriptions
    @SerializedName("transactionId")
    val transactionId: String,

    @SerializedName("purchaseToken")
    val purchaseToken: String? = null,

    @SerializedName("transactionDate")
    val transactionDate: Long,

    @SerializedName("platform")
    val platform: String = "android",

    // Android-only fields
    @SerializedName("autoRenewingAndroid")
    val autoRenewingAndroid: Boolean? = null,

    // iOS-only fields kept for cross-platform union typing (always null on Android)
    @SerializedName("expirationDateIOS")
    val expirationDateIOS: Long? = null,

    @SerializedName("environmentIOS")
    val environmentIOS: String? = null,

    @SerializedName("willExpireSoon")
    val willExpireSoon: Boolean? = null,

    @SerializedName("daysUntilExpirationIOS")
    val daysUntilExpirationIOS: Number? = null
)
