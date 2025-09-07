package dev.hyo.openiap.models

import com.google.gson.annotations.SerializedName

/**
 * Active subscription information per OpenIAP spec (Android variant)
 */
data class ActiveSubscription(
    @SerializedName("productId")
    val productId: String,

    @SerializedName("isActive")
    val isActive: Boolean,

    @SerializedName("platform")
    val platform: String = "android",

    // Android-only fields
    @SerializedName("autoRenewingAndroid")
    val autoRenewingAndroid: Boolean? = null,

    // iOS-only fields kept for cross-platform union typing (always null on Android)
    @SerializedName("expirationDateIOS")
    val expirationDateIOS: String? = null,

    @SerializedName("environmentIOS")
    val environmentIOS: String? = null,

    @SerializedName("willExpireSoon")
    val willExpireSoon: Boolean? = null,

    @SerializedName("daysUntilExpirationIOS")
    val daysUntilExpirationIOS: Number? = null
)
