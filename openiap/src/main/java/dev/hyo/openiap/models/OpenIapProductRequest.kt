package dev.hyo.openiap.models

/**
 * Product request parameters for fetching products from the store
 */
data class OpenIapProductRequest(
    val skus: List<String>,
    val type: ProductRequestType = ProductRequestType.InApp
) {
    enum class ProductRequestType(val value: String) {
        InApp("in-app"),
        Subs("subs"),
        All("all");

        companion object {
            private val INAPP_ALIASES = setOf(
                "in-app",
                "inapp", // Legacy alias slated for removal in 1.2.0
            )

            fun fromString(value: String): ProductRequestType {
                val normalized = value.lowercase()
                return when {
                    normalized in INAPP_ALIASES -> InApp
                    normalized == Subs.value -> Subs
                    normalized == All.value -> All
                    else -> InApp
                }
            }
        }
    }
}
