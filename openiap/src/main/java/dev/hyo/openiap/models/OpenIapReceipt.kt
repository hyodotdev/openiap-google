package dev.hyo.openiap.models

/**
 * Cross-platform receipt representation placeholder.
 * On Android, Google Play Billing v6+ does not expose a receipt blob like iOS;
 * the purchase token and originalJson are used for server validation.
 */
data class OpenIapReceipt(
    val productId: String,
    val purchaseToken: String?,
    val originalJson: String?
)

