package dev.hyo.openiap.models

/**
 * Product request parameters for fetching products from the store
 */
data class OpenIapProductRequest(
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
