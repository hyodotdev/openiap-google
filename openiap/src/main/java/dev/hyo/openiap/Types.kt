// ============================================================================
// AUTO-GENERATED TYPES — DO NOT EDIT DIRECTLY
// Run `npm run generate` after updating any *.graphql schema file.
// ============================================================================

@file:Suppress("unused")
package dev.hyo.openiap

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put

private fun describeJsonType(value: JsonElement?): String = when (value) {
    null -> "absent"
    JsonNull -> "null"
    is JsonObject -> "object"
    is JsonArray -> "array"
    is JsonPrimitive -> when {
        value.isString -> "string"
        value.booleanOrNull != null -> "boolean"
        value.doubleOrNull != null -> "number"
        else -> "primitive"
    }
    else -> "unknown"
}

private fun isNullish(element: JsonElement?): Boolean = element == null || element === JsonNull

private fun requireJsonObject(element: JsonElement?, fieldName: String): JsonObject {
    if (element is JsonObject) return element
    throw IllegalArgumentException("Expected `${fieldName}` to be an object but was ${describeJsonType(element)}")
}

private fun optionalJsonObject(element: JsonElement?, fieldName: String): JsonObject? {
    if (isNullish(element)) return null
    if (element is JsonObject) return element
    throw IllegalArgumentException("Expected `${fieldName}` to be an object but was ${describeJsonType(element)}")
}

private fun requireJsonArray(element: JsonElement?, fieldName: String): JsonArray {
    if (element is JsonArray) return element
    throw IllegalArgumentException("Expected `${fieldName}` to be an array but was ${describeJsonType(element)}")
}

private fun optionalJsonArray(element: JsonElement?, fieldName: String): JsonArray? {
    if (isNullish(element)) return null
    if (element is JsonArray) return element
    throw IllegalArgumentException("Expected `${fieldName}` to be an array but was ${describeJsonType(element)}")
}

private fun requireJsonPrimitive(element: JsonElement?, fieldName: String): JsonPrimitive {
    if (element is JsonPrimitive && element !== JsonNull) return element
    throw IllegalArgumentException("Expected `${fieldName}` to be a primitive but was ${describeJsonType(element)}")
}

private fun optionalJsonPrimitive(element: JsonElement?, fieldName: String): JsonPrimitive? {
    if (isNullish(element)) return null
    if (element is JsonPrimitive && element !== JsonNull) return element
    throw IllegalArgumentException("Expected `${fieldName}` to be a primitive but was ${describeJsonType(element)}")
}

private fun requireJsonString(element: JsonElement?, fieldName: String): String {
    val primitive = requireJsonPrimitive(element, fieldName)
    if (!primitive.isString) {
        throw IllegalArgumentException("Expected `${fieldName}` to be a string but was ${describeJsonType(primitive)}")
    }
    return primitive.content
}

private fun optionalJsonString(element: JsonElement?, fieldName: String): String? {
    val primitive = optionalJsonPrimitive(element, fieldName) ?: return null
    if (!primitive.isString) {
        throw IllegalArgumentException("Expected `${fieldName}` to be a string but was ${describeJsonType(primitive)}")
    }
    return primitive.content
}

private fun requireJsonBoolean(element: JsonElement?, fieldName: String): Boolean {
    val primitive = requireJsonPrimitive(element, fieldName)
    return primitive.booleanOrNull ?: throw IllegalArgumentException("Expected `${fieldName}` to be a boolean but was ${describeJsonType(primitive)}")
}

private fun optionalJsonBoolean(element: JsonElement?, fieldName: String): Boolean? {
    val primitive = optionalJsonPrimitive(element, fieldName) ?: return null
    return primitive.booleanOrNull ?: throw IllegalArgumentException("Expected `${fieldName}` to be a boolean but was ${describeJsonType(primitive)}")
}

private fun requireJsonInt(element: JsonElement?, fieldName: String): Int {
    val primitive = requireJsonPrimitive(element, fieldName)
    return primitive.intOrNull ?: throw IllegalArgumentException("Expected `${fieldName}` to be an integer but was ${describeJsonType(primitive)}")
}

private fun optionalJsonInt(element: JsonElement?, fieldName: String): Int? {
    val primitive = optionalJsonPrimitive(element, fieldName) ?: return null
    return primitive.intOrNull ?: throw IllegalArgumentException("Expected `${fieldName}` to be an integer but was ${describeJsonType(primitive)}")
}

private fun requireJsonDouble(element: JsonElement?, fieldName: String): Double {
    val primitive = requireJsonPrimitive(element, fieldName)
    return primitive.doubleOrNull ?: throw IllegalArgumentException("Expected `${fieldName}` to be a number but was ${describeJsonType(primitive)}")
}

private fun optionalJsonDouble(element: JsonElement?, fieldName: String): Double? {
    val primitive = optionalJsonPrimitive(element, fieldName) ?: return null
    return primitive.doubleOrNull ?: throw IllegalArgumentException("Expected `${fieldName}` to be a number but was ${describeJsonType(primitive)}")
}

// MARK: - Enums

public enum class ErrorCode(val rawValue: String) {
    Unknown("unknown"),
    UserCancelled("user-cancelled"),
    UserError("user-error"),
    ItemUnavailable("item-unavailable"),
    RemoteError("remote-error"),
    NetworkError("network-error"),
    ServiceError("service-error"),
    ReceiptFailed("receipt-failed"),
    ReceiptFinished("receipt-finished"),
    ReceiptFinishedFailed("receipt-finished-failed"),
    NotPrepared("not-prepared"),
    NotEnded("not-ended"),
    AlreadyOwned("already-owned"),
    DeveloperError("developer-error"),
    BillingResponseJsonParseError("billing-response-json-parse-error"),
    DeferredPayment("deferred-payment"),
    Interrupted("interrupted"),
    IapNotAvailable("iap-not-available"),
    PurchaseError("purchase-error"),
    SyncError("sync-error"),
    TransactionValidationFailed("transaction-validation-failed"),
    ActivityUnavailable("activity-unavailable"),
    AlreadyPrepared("already-prepared"),
    Pending("pending"),
    ConnectionClosed("connection-closed"),
    InitConnection("init-connection"),
    ServiceDisconnected("service-disconnected"),
    QueryProduct("query-product"),
    SkuNotFound("sku-not-found"),
    SkuOfferMismatch("sku-offer-mismatch"),
    ItemNotOwned("item-not-owned"),
    BillingUnavailable("billing-unavailable"),
    FeatureNotSupported("feature-not-supported"),
    EmptySkuList("empty-sku-list");

    companion object {
        fun fromJson(value: String): ErrorCode = when (value) {
            "unknown" -> ErrorCode.Unknown
            "Unknown" -> ErrorCode.Unknown
            "user-cancelled" -> ErrorCode.UserCancelled
            "UserCancelled" -> ErrorCode.UserCancelled
            "user-error" -> ErrorCode.UserError
            "UserError" -> ErrorCode.UserError
            "item-unavailable" -> ErrorCode.ItemUnavailable
            "ItemUnavailable" -> ErrorCode.ItemUnavailable
            "remote-error" -> ErrorCode.RemoteError
            "RemoteError" -> ErrorCode.RemoteError
            "network-error" -> ErrorCode.NetworkError
            "NetworkError" -> ErrorCode.NetworkError
            "service-error" -> ErrorCode.ServiceError
            "ServiceError" -> ErrorCode.ServiceError
            "receipt-failed" -> ErrorCode.ReceiptFailed
            "ReceiptFailed" -> ErrorCode.ReceiptFailed
            "receipt-finished" -> ErrorCode.ReceiptFinished
            "ReceiptFinished" -> ErrorCode.ReceiptFinished
            "receipt-finished-failed" -> ErrorCode.ReceiptFinishedFailed
            "ReceiptFinishedFailed" -> ErrorCode.ReceiptFinishedFailed
            "not-prepared" -> ErrorCode.NotPrepared
            "NotPrepared" -> ErrorCode.NotPrepared
            "not-ended" -> ErrorCode.NotEnded
            "NotEnded" -> ErrorCode.NotEnded
            "already-owned" -> ErrorCode.AlreadyOwned
            "AlreadyOwned" -> ErrorCode.AlreadyOwned
            "developer-error" -> ErrorCode.DeveloperError
            "DeveloperError" -> ErrorCode.DeveloperError
            "billing-response-json-parse-error" -> ErrorCode.BillingResponseJsonParseError
            "BillingResponseJsonParseError" -> ErrorCode.BillingResponseJsonParseError
            "deferred-payment" -> ErrorCode.DeferredPayment
            "DeferredPayment" -> ErrorCode.DeferredPayment
            "interrupted" -> ErrorCode.Interrupted
            "Interrupted" -> ErrorCode.Interrupted
            "iap-not-available" -> ErrorCode.IapNotAvailable
            "IapNotAvailable" -> ErrorCode.IapNotAvailable
            "purchase-error" -> ErrorCode.PurchaseError
            "PurchaseError" -> ErrorCode.PurchaseError
            "sync-error" -> ErrorCode.SyncError
            "SyncError" -> ErrorCode.SyncError
            "transaction-validation-failed" -> ErrorCode.TransactionValidationFailed
            "TransactionValidationFailed" -> ErrorCode.TransactionValidationFailed
            "activity-unavailable" -> ErrorCode.ActivityUnavailable
            "ActivityUnavailable" -> ErrorCode.ActivityUnavailable
            "already-prepared" -> ErrorCode.AlreadyPrepared
            "AlreadyPrepared" -> ErrorCode.AlreadyPrepared
            "pending" -> ErrorCode.Pending
            "Pending" -> ErrorCode.Pending
            "connection-closed" -> ErrorCode.ConnectionClosed
            "ConnectionClosed" -> ErrorCode.ConnectionClosed
            "init-connection" -> ErrorCode.InitConnection
            "InitConnection" -> ErrorCode.InitConnection
            "service-disconnected" -> ErrorCode.ServiceDisconnected
            "ServiceDisconnected" -> ErrorCode.ServiceDisconnected
            "query-product" -> ErrorCode.QueryProduct
            "QueryProduct" -> ErrorCode.QueryProduct
            "sku-not-found" -> ErrorCode.SkuNotFound
            "SkuNotFound" -> ErrorCode.SkuNotFound
            "sku-offer-mismatch" -> ErrorCode.SkuOfferMismatch
            "SkuOfferMismatch" -> ErrorCode.SkuOfferMismatch
            "item-not-owned" -> ErrorCode.ItemNotOwned
            "ItemNotOwned" -> ErrorCode.ItemNotOwned
            "billing-unavailable" -> ErrorCode.BillingUnavailable
            "BillingUnavailable" -> ErrorCode.BillingUnavailable
            "feature-not-supported" -> ErrorCode.FeatureNotSupported
            "FeatureNotSupported" -> ErrorCode.FeatureNotSupported
            "empty-sku-list" -> ErrorCode.EmptySkuList
            "EmptySkuList" -> ErrorCode.EmptySkuList
            else -> throw IllegalArgumentException("Unknown ErrorCode value: $value")
        }
    }

    fun toJson(): String = rawValue
}

public enum class IapEvent(val rawValue: String) {
    PurchaseUpdated("purchase-updated"),
    PurchaseError("purchase-error"),
    PromotedProductIos("promoted-product-ios");

    companion object {
        fun fromJson(value: String): IapEvent = when (value) {
            "purchase-updated" -> IapEvent.PurchaseUpdated
            "PURCHASE_UPDATED" -> IapEvent.PurchaseUpdated
            "PurchaseUpdated" -> IapEvent.PurchaseUpdated
            "purchase-error" -> IapEvent.PurchaseError
            "PURCHASE_ERROR" -> IapEvent.PurchaseError
            "PurchaseError" -> IapEvent.PurchaseError
            "promoted-product-ios" -> IapEvent.PromotedProductIos
            "PROMOTED_PRODUCT_IOS" -> IapEvent.PromotedProductIos
            "PromotedProductIOS" -> IapEvent.PromotedProductIos
            else -> throw IllegalArgumentException("Unknown IapEvent value: $value")
        }
    }

    fun toJson(): String = rawValue
}

public enum class IapPlatform(val rawValue: String) {
    Ios("ios"),
    Android("android");

    companion object {
        fun fromJson(value: String): IapPlatform = when (value) {
            "ios" -> IapPlatform.Ios
            "IOS" -> IapPlatform.Ios
            "android" -> IapPlatform.Android
            "ANDROID" -> IapPlatform.Android
            "Android" -> IapPlatform.Android
            else -> throw IllegalArgumentException("Unknown IapPlatform value: $value")
        }
    }

    fun toJson(): String = rawValue
}

public enum class PaymentModeIOS(val rawValue: String) {
    Empty("empty"),
    FreeTrial("free-trial"),
    PayAsYouGo("pay-as-you-go"),
    PayUpFront("pay-up-front");

    companion object {
        fun fromJson(value: String): PaymentModeIOS = when (value) {
            "empty" -> PaymentModeIOS.Empty
            "Empty" -> PaymentModeIOS.Empty
            "free-trial" -> PaymentModeIOS.FreeTrial
            "FreeTrial" -> PaymentModeIOS.FreeTrial
            "pay-as-you-go" -> PaymentModeIOS.PayAsYouGo
            "PayAsYouGo" -> PaymentModeIOS.PayAsYouGo
            "pay-up-front" -> PaymentModeIOS.PayUpFront
            "PayUpFront" -> PaymentModeIOS.PayUpFront
            else -> throw IllegalArgumentException("Unknown PaymentModeIOS value: $value")
        }
    }

    fun toJson(): String = rawValue
}

public enum class ProductQueryType(val rawValue: String) {
    InApp("in-app"),
    Subs("subs"),
    All("all");

    companion object {
        fun fromJson(value: String): ProductQueryType = when (value) {
            "in-app" -> ProductQueryType.InApp
            "InApp" -> ProductQueryType.InApp
            "subs" -> ProductQueryType.Subs
            "Subs" -> ProductQueryType.Subs
            "all" -> ProductQueryType.All
            "All" -> ProductQueryType.All
            else -> throw IllegalArgumentException("Unknown ProductQueryType value: $value")
        }
    }

    fun toJson(): String = rawValue
}

public enum class ProductType(val rawValue: String) {
    InApp("in-app"),
    Subs("subs");

    companion object {
        fun fromJson(value: String): ProductType = when (value) {
            "in-app" -> ProductType.InApp
            "InApp" -> ProductType.InApp
            "subs" -> ProductType.Subs
            "Subs" -> ProductType.Subs
            else -> throw IllegalArgumentException("Unknown ProductType value: $value")
        }
    }

    fun toJson(): String = rawValue
}

public enum class ProductTypeIOS(val rawValue: String) {
    Consumable("consumable"),
    NonConsumable("non-consumable"),
    AutoRenewableSubscription("auto-renewable-subscription"),
    NonRenewingSubscription("non-renewing-subscription");

    companion object {
        fun fromJson(value: String): ProductTypeIOS = when (value) {
            "consumable" -> ProductTypeIOS.Consumable
            "Consumable" -> ProductTypeIOS.Consumable
            "non-consumable" -> ProductTypeIOS.NonConsumable
            "NonConsumable" -> ProductTypeIOS.NonConsumable
            "auto-renewable-subscription" -> ProductTypeIOS.AutoRenewableSubscription
            "AutoRenewableSubscription" -> ProductTypeIOS.AutoRenewableSubscription
            "non-renewing-subscription" -> ProductTypeIOS.NonRenewingSubscription
            "NonRenewingSubscription" -> ProductTypeIOS.NonRenewingSubscription
            else -> throw IllegalArgumentException("Unknown ProductTypeIOS value: $value")
        }
    }

    fun toJson(): String = rawValue
}

public enum class PurchaseState(val rawValue: String) {
    Pending("pending"),
    Purchased("purchased"),
    Failed("failed"),
    Restored("restored"),
    Deferred("deferred"),
    Unknown("unknown");

    companion object {
        fun fromJson(value: String): PurchaseState = when (value) {
            "pending" -> PurchaseState.Pending
            "Pending" -> PurchaseState.Pending
            "purchased" -> PurchaseState.Purchased
            "Purchased" -> PurchaseState.Purchased
            "failed" -> PurchaseState.Failed
            "Failed" -> PurchaseState.Failed
            "restored" -> PurchaseState.Restored
            "Restored" -> PurchaseState.Restored
            "deferred" -> PurchaseState.Deferred
            "Deferred" -> PurchaseState.Deferred
            "unknown" -> PurchaseState.Unknown
            "Unknown" -> PurchaseState.Unknown
            else -> throw IllegalArgumentException("Unknown PurchaseState value: $value")
        }
    }

    fun toJson(): String = rawValue
}

public enum class SubscriptionOfferTypeIOS(val rawValue: String) {
    Introductory("introductory"),
    Promotional("promotional");

    companion object {
        fun fromJson(value: String): SubscriptionOfferTypeIOS = when (value) {
            "introductory" -> SubscriptionOfferTypeIOS.Introductory
            "Introductory" -> SubscriptionOfferTypeIOS.Introductory
            "promotional" -> SubscriptionOfferTypeIOS.Promotional
            "Promotional" -> SubscriptionOfferTypeIOS.Promotional
            else -> throw IllegalArgumentException("Unknown SubscriptionOfferTypeIOS value: $value")
        }
    }

    fun toJson(): String = rawValue
}

public enum class SubscriptionPeriodIOS(val rawValue: String) {
    Day("day"),
    Week("week"),
    Month("month"),
    Year("year"),
    Empty("empty");

    companion object {
        fun fromJson(value: String): SubscriptionPeriodIOS = when (value) {
            "day" -> SubscriptionPeriodIOS.Day
            "Day" -> SubscriptionPeriodIOS.Day
            "week" -> SubscriptionPeriodIOS.Week
            "Week" -> SubscriptionPeriodIOS.Week
            "month" -> SubscriptionPeriodIOS.Month
            "Month" -> SubscriptionPeriodIOS.Month
            "year" -> SubscriptionPeriodIOS.Year
            "Year" -> SubscriptionPeriodIOS.Year
            "empty" -> SubscriptionPeriodIOS.Empty
            "Empty" -> SubscriptionPeriodIOS.Empty
            else -> throw IllegalArgumentException("Unknown SubscriptionPeriodIOS value: $value")
        }
    }

    fun toJson(): String = rawValue
}

// MARK: - Interfaces

public interface ProductCommon {
    val currency: String
    val debugDescription: String?
    val description: String
    val displayName: String?
    val displayPrice: String
    val id: String
    val platform: IapPlatform
    val price: Double?
    val title: String
    val type: ProductType
}

public interface PurchaseCommon {
    val id: String
    val ids: List<String>?
    val isAutoRenewing: Boolean
    val platform: IapPlatform
    val productId: String
    val purchaseState: PurchaseState
    /**
     * Unified purchase token (iOS JWS, Android purchaseToken)
     */
    val purchaseToken: String?
    val quantity: Int
    val transactionDate: Double
}

// MARK: - Objects

public data class ActiveSubscription(
    val autoRenewingAndroid: Boolean? = null,
    val daysUntilExpirationIOS: Double? = null,
    val environmentIOS: String? = null,
    val expirationDateIOS: Double? = null,
    val isActive: Boolean,
    val productId: String,
    val purchaseToken: String? = null,
    val transactionDate: Double,
    val transactionId: String,
    val willExpireSoon: Boolean? = null
) {

    companion object {
        fun fromJson(json: JsonObject): ActiveSubscription {
            return ActiveSubscription(
                autoRenewingAndroid = optionalJsonBoolean(json["autoRenewingAndroid"], "autoRenewingAndroid"),
                daysUntilExpirationIOS = optionalJsonDouble(json["daysUntilExpirationIOS"], "daysUntilExpirationIOS"),
                environmentIOS = optionalJsonString(json["environmentIOS"], "environmentIOS"),
                expirationDateIOS = optionalJsonDouble(json["expirationDateIOS"], "expirationDateIOS"),
                isActive = requireJsonBoolean(json["isActive"], "isActive"),
                productId = requireJsonString(json["productId"], "productId"),
                purchaseToken = optionalJsonString(json["purchaseToken"], "purchaseToken"),
                transactionDate = requireJsonDouble(json["transactionDate"], "transactionDate"),
                transactionId = requireJsonString(json["transactionId"], "transactionId"),
                willExpireSoon = optionalJsonBoolean(json["willExpireSoon"], "willExpireSoon"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "ActiveSubscription")
        put("autoRenewingAndroid", autoRenewingAndroid?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("daysUntilExpirationIOS", daysUntilExpirationIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("environmentIOS", environmentIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("expirationDateIOS", expirationDateIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("isActive", JsonPrimitive(isActive))
        put("productId", JsonPrimitive(productId))
        put("purchaseToken", purchaseToken?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("transactionDate", JsonPrimitive(transactionDate))
        put("transactionId", JsonPrimitive(transactionId))
        put("willExpireSoon", willExpireSoon?.let { value -> JsonPrimitive(value) } ?: JsonNull)
    }
}

public data class AppTransaction(
    val appId: Double,
    val appTransactionId: String? = null,
    val appVersion: String,
    val appVersionId: Double,
    val bundleId: String,
    val deviceVerification: String,
    val deviceVerificationNonce: String,
    val environment: String,
    val originalAppVersion: String,
    val originalPlatform: String? = null,
    val originalPurchaseDate: Double,
    val preorderDate: Double? = null,
    val signedDate: Double
) {

    companion object {
        fun fromJson(json: JsonObject): AppTransaction {
            return AppTransaction(
                appId = requireJsonDouble(json["appId"], "appId"),
                appTransactionId = optionalJsonString(json["appTransactionId"], "appTransactionId"),
                appVersion = requireJsonString(json["appVersion"], "appVersion"),
                appVersionId = requireJsonDouble(json["appVersionId"], "appVersionId"),
                bundleId = requireJsonString(json["bundleId"], "bundleId"),
                deviceVerification = requireJsonString(json["deviceVerification"], "deviceVerification"),
                deviceVerificationNonce = requireJsonString(json["deviceVerificationNonce"], "deviceVerificationNonce"),
                environment = requireJsonString(json["environment"], "environment"),
                originalAppVersion = requireJsonString(json["originalAppVersion"], "originalAppVersion"),
                originalPlatform = optionalJsonString(json["originalPlatform"], "originalPlatform"),
                originalPurchaseDate = requireJsonDouble(json["originalPurchaseDate"], "originalPurchaseDate"),
                preorderDate = optionalJsonDouble(json["preorderDate"], "preorderDate"),
                signedDate = requireJsonDouble(json["signedDate"], "signedDate"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "AppTransaction")
        put("appId", JsonPrimitive(appId))
        put("appTransactionId", appTransactionId?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("appVersion", JsonPrimitive(appVersion))
        put("appVersionId", JsonPrimitive(appVersionId))
        put("bundleId", JsonPrimitive(bundleId))
        put("deviceVerification", JsonPrimitive(deviceVerification))
        put("deviceVerificationNonce", JsonPrimitive(deviceVerificationNonce))
        put("environment", JsonPrimitive(environment))
        put("originalAppVersion", JsonPrimitive(originalAppVersion))
        put("originalPlatform", originalPlatform?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("originalPurchaseDate", JsonPrimitive(originalPurchaseDate))
        put("preorderDate", preorderDate?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("signedDate", JsonPrimitive(signedDate))
    }
}

public data class DiscountIOS(
    val identifier: String,
    val localizedPrice: String? = null,
    val numberOfPeriods: Int,
    val paymentMode: PaymentModeIOS,
    val price: String,
    val priceAmount: Double,
    val subscriptionPeriod: String,
    val type: String
) {

    companion object {
        fun fromJson(json: JsonObject): DiscountIOS {
            return DiscountIOS(
                identifier = requireJsonString(json["identifier"], "identifier"),
                localizedPrice = optionalJsonString(json["localizedPrice"], "localizedPrice"),
                numberOfPeriods = requireJsonInt(json["numberOfPeriods"], "numberOfPeriods"),
                paymentMode = PaymentModeIOS.fromJson(requireJsonString(json["paymentMode"], "paymentMode")),
                price = requireJsonString(json["price"], "price"),
                priceAmount = requireJsonDouble(json["priceAmount"], "priceAmount"),
                subscriptionPeriod = requireJsonString(json["subscriptionPeriod"], "subscriptionPeriod"),
                type = requireJsonString(json["type"], "type"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "DiscountIOS")
        put("identifier", JsonPrimitive(identifier))
        put("localizedPrice", localizedPrice?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("numberOfPeriods", JsonPrimitive(numberOfPeriods))
        put("paymentMode", JsonPrimitive(paymentMode.toJson()))
        put("price", JsonPrimitive(price))
        put("priceAmount", JsonPrimitive(priceAmount))
        put("subscriptionPeriod", JsonPrimitive(subscriptionPeriod))
        put("type", JsonPrimitive(type))
    }
}

public data class DiscountOfferIOS(
    /**
     * Discount identifier
     */
    val identifier: String,
    /**
     * Key identifier for validation
     */
    val keyIdentifier: String,
    /**
     * Cryptographic nonce
     */
    val nonce: String,
    /**
     * Signature for validation
     */
    val signature: String,
    /**
     * Timestamp of discount offer
     */
    val timestamp: Double
) {

    companion object {
        fun fromJson(json: JsonObject): DiscountOfferIOS {
            return DiscountOfferIOS(
                identifier = requireJsonString(json["identifier"], "identifier"),
                keyIdentifier = requireJsonString(json["keyIdentifier"], "keyIdentifier"),
                nonce = requireJsonString(json["nonce"], "nonce"),
                signature = requireJsonString(json["signature"], "signature"),
                timestamp = requireJsonDouble(json["timestamp"], "timestamp"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "DiscountOfferIOS")
        put("identifier", JsonPrimitive(identifier))
        put("keyIdentifier", JsonPrimitive(keyIdentifier))
        put("nonce", JsonPrimitive(nonce))
        put("signature", JsonPrimitive(signature))
        put("timestamp", JsonPrimitive(timestamp))
    }
}

public data class EntitlementIOS(
    val jsonRepresentation: String,
    val sku: String,
    val transactionId: String
) {

    companion object {
        fun fromJson(json: JsonObject): EntitlementIOS {
            return EntitlementIOS(
                jsonRepresentation = requireJsonString(json["jsonRepresentation"], "jsonRepresentation"),
                sku = requireJsonString(json["sku"], "sku"),
                transactionId = requireJsonString(json["transactionId"], "transactionId"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "EntitlementIOS")
        put("jsonRepresentation", JsonPrimitive(jsonRepresentation))
        put("sku", JsonPrimitive(sku))
        put("transactionId", JsonPrimitive(transactionId))
    }
}

public sealed interface FetchProductsResult

public data class FetchProductsResultProducts(val value: List<Product>?) : FetchProductsResult

public data class FetchProductsResultSubscriptions(val value: List<ProductSubscription>?) : FetchProductsResult

public data class PricingPhaseAndroid(
    val billingCycleCount: Int,
    val billingPeriod: String,
    val formattedPrice: String,
    val priceAmountMicros: String,
    val priceCurrencyCode: String,
    val recurrenceMode: Int
) {

    companion object {
        fun fromJson(json: JsonObject): PricingPhaseAndroid {
            return PricingPhaseAndroid(
                billingCycleCount = requireJsonInt(json["billingCycleCount"], "billingCycleCount"),
                billingPeriod = requireJsonString(json["billingPeriod"], "billingPeriod"),
                formattedPrice = requireJsonString(json["formattedPrice"], "formattedPrice"),
                priceAmountMicros = requireJsonString(json["priceAmountMicros"], "priceAmountMicros"),
                priceCurrencyCode = requireJsonString(json["priceCurrencyCode"], "priceCurrencyCode"),
                recurrenceMode = requireJsonInt(json["recurrenceMode"], "recurrenceMode"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "PricingPhaseAndroid")
        put("billingCycleCount", JsonPrimitive(billingCycleCount))
        put("billingPeriod", JsonPrimitive(billingPeriod))
        put("formattedPrice", JsonPrimitive(formattedPrice))
        put("priceAmountMicros", JsonPrimitive(priceAmountMicros))
        put("priceCurrencyCode", JsonPrimitive(priceCurrencyCode))
        put("recurrenceMode", JsonPrimitive(recurrenceMode))
    }
}

public data class PricingPhasesAndroid(
    val pricingPhaseList: List<PricingPhaseAndroid>
) {

    companion object {
        fun fromJson(json: JsonObject): PricingPhasesAndroid {
            return PricingPhasesAndroid(
                pricingPhaseList = requireJsonArray(json["pricingPhaseList"], "pricingPhaseList").map { element -> PricingPhaseAndroid.fromJson(requireJsonObject(element, "pricingPhaseList[]")) },
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "PricingPhasesAndroid")
        put("pricingPhaseList", buildJsonArray { pricingPhaseList.forEach { element -> add(element.toJson()) } })
    }
}

public data class ProductAndroid(
    override val currency: String,
    override val debugDescription: String? = null,
    override val description: String,
    override val displayName: String? = null,
    override val displayPrice: String,
    override val id: String,
    val nameAndroid: String,
    val oneTimePurchaseOfferDetailsAndroid: ProductAndroidOneTimePurchaseOfferDetail? = null,
    override val platform: IapPlatform,
    override val price: Double? = null,
    val subscriptionOfferDetailsAndroid: List<ProductSubscriptionAndroidOfferDetails>? = null,
    override val title: String,
    override val type: ProductType
) : ProductCommon, Product {

    companion object {
        fun fromJson(json: JsonObject): ProductAndroid {
            return ProductAndroid(
                currency = requireJsonString(json["currency"], "currency"),
                debugDescription = optionalJsonString(json["debugDescription"], "debugDescription"),
                description = requireJsonString(json["description"], "description"),
                displayName = optionalJsonString(json["displayName"], "displayName"),
                displayPrice = requireJsonString(json["displayPrice"], "displayPrice"),
                id = requireJsonString(json["id"], "id"),
                nameAndroid = requireJsonString(json["nameAndroid"], "nameAndroid"),
                oneTimePurchaseOfferDetailsAndroid = optionalJsonObject(json["oneTimePurchaseOfferDetailsAndroid"], "oneTimePurchaseOfferDetailsAndroid")?.let { ProductAndroidOneTimePurchaseOfferDetail.fromJson(it) },
                platform = IapPlatform.fromJson(requireJsonString(json["platform"], "platform")),
                price = optionalJsonDouble(json["price"], "price"),
                subscriptionOfferDetailsAndroid = optionalJsonArray(json["subscriptionOfferDetailsAndroid"], "subscriptionOfferDetailsAndroid")?.map { element -> ProductSubscriptionAndroidOfferDetails.fromJson(requireJsonObject(element, "subscriptionOfferDetailsAndroid[]")) },
                title = requireJsonString(json["title"], "title"),
                type = ProductType.fromJson(requireJsonString(json["type"], "type")),
            )
        }
    }

    override fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "ProductAndroid")
        put("currency", JsonPrimitive(currency))
        put("debugDescription", debugDescription?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("description", JsonPrimitive(description))
        put("displayName", displayName?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("displayPrice", JsonPrimitive(displayPrice))
        put("id", JsonPrimitive(id))
        put("nameAndroid", JsonPrimitive(nameAndroid))
        put("oneTimePurchaseOfferDetailsAndroid", oneTimePurchaseOfferDetailsAndroid?.let { value -> value.toJson() } ?: JsonNull)
        put("platform", JsonPrimitive(platform.toJson()))
        put("price", price?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("subscriptionOfferDetailsAndroid", subscriptionOfferDetailsAndroid?.let { value -> buildJsonArray { value.forEach { element -> add(element.toJson()) } } } ?: JsonNull)
        put("title", JsonPrimitive(title))
        put("type", JsonPrimitive(type.toJson()))
    }
}

public data class ProductAndroidOneTimePurchaseOfferDetail(
    val formattedPrice: String,
    val priceAmountMicros: String,
    val priceCurrencyCode: String
) {

    companion object {
        fun fromJson(json: JsonObject): ProductAndroidOneTimePurchaseOfferDetail {
            return ProductAndroidOneTimePurchaseOfferDetail(
                formattedPrice = requireJsonString(json["formattedPrice"], "formattedPrice"),
                priceAmountMicros = requireJsonString(json["priceAmountMicros"], "priceAmountMicros"),
                priceCurrencyCode = requireJsonString(json["priceCurrencyCode"], "priceCurrencyCode"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "ProductAndroidOneTimePurchaseOfferDetail")
        put("formattedPrice", JsonPrimitive(formattedPrice))
        put("priceAmountMicros", JsonPrimitive(priceAmountMicros))
        put("priceCurrencyCode", JsonPrimitive(priceCurrencyCode))
    }
}

public data class ProductIOS(
    override val currency: String,
    override val debugDescription: String? = null,
    override val description: String,
    override val displayName: String? = null,
    val displayNameIOS: String,
    override val displayPrice: String,
    override val id: String,
    val isFamilyShareableIOS: Boolean,
    val jsonRepresentationIOS: String,
    override val platform: IapPlatform,
    override val price: Double? = null,
    val subscriptionInfoIOS: SubscriptionInfoIOS? = null,
    override val title: String,
    override val type: ProductType,
    val typeIOS: ProductTypeIOS
) : ProductCommon, Product {

    companion object {
        fun fromJson(json: JsonObject): ProductIOS {
            return ProductIOS(
                currency = requireJsonString(json["currency"], "currency"),
                debugDescription = optionalJsonString(json["debugDescription"], "debugDescription"),
                description = requireJsonString(json["description"], "description"),
                displayName = optionalJsonString(json["displayName"], "displayName"),
                displayNameIOS = requireJsonString(json["displayNameIOS"], "displayNameIOS"),
                displayPrice = requireJsonString(json["displayPrice"], "displayPrice"),
                id = requireJsonString(json["id"], "id"),
                isFamilyShareableIOS = requireJsonBoolean(json["isFamilyShareableIOS"], "isFamilyShareableIOS"),
                jsonRepresentationIOS = requireJsonString(json["jsonRepresentationIOS"], "jsonRepresentationIOS"),
                platform = IapPlatform.fromJson(requireJsonString(json["platform"], "platform")),
                price = optionalJsonDouble(json["price"], "price"),
                subscriptionInfoIOS = optionalJsonObject(json["subscriptionInfoIOS"], "subscriptionInfoIOS")?.let { SubscriptionInfoIOS.fromJson(it) },
                title = requireJsonString(json["title"], "title"),
                type = ProductType.fromJson(requireJsonString(json["type"], "type")),
                typeIOS = ProductTypeIOS.fromJson(requireJsonString(json["typeIOS"], "typeIOS")),
            )
        }
    }

    override fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "ProductIOS")
        put("currency", JsonPrimitive(currency))
        put("debugDescription", debugDescription?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("description", JsonPrimitive(description))
        put("displayName", displayName?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("displayNameIOS", JsonPrimitive(displayNameIOS))
        put("displayPrice", JsonPrimitive(displayPrice))
        put("id", JsonPrimitive(id))
        put("isFamilyShareableIOS", JsonPrimitive(isFamilyShareableIOS))
        put("jsonRepresentationIOS", JsonPrimitive(jsonRepresentationIOS))
        put("platform", JsonPrimitive(platform.toJson()))
        put("price", price?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("subscriptionInfoIOS", subscriptionInfoIOS?.let { value -> value.toJson() } ?: JsonNull)
        put("title", JsonPrimitive(title))
        put("type", JsonPrimitive(type.toJson()))
        put("typeIOS", JsonPrimitive(typeIOS.toJson()))
    }
}

public data class ProductSubscriptionAndroid(
    override val currency: String,
    override val debugDescription: String? = null,
    override val description: String,
    override val displayName: String? = null,
    override val displayPrice: String,
    override val id: String,
    val nameAndroid: String,
    val oneTimePurchaseOfferDetailsAndroid: ProductAndroidOneTimePurchaseOfferDetail? = null,
    override val platform: IapPlatform,
    override val price: Double? = null,
    val subscriptionOfferDetailsAndroid: List<ProductSubscriptionAndroidOfferDetails>,
    override val title: String,
    override val type: ProductType
) : ProductCommon, ProductSubscription {

    companion object {
        fun fromJson(json: JsonObject): ProductSubscriptionAndroid {
            return ProductSubscriptionAndroid(
                currency = requireJsonString(json["currency"], "currency"),
                debugDescription = optionalJsonString(json["debugDescription"], "debugDescription"),
                description = requireJsonString(json["description"], "description"),
                displayName = optionalJsonString(json["displayName"], "displayName"),
                displayPrice = requireJsonString(json["displayPrice"], "displayPrice"),
                id = requireJsonString(json["id"], "id"),
                nameAndroid = requireJsonString(json["nameAndroid"], "nameAndroid"),
                oneTimePurchaseOfferDetailsAndroid = optionalJsonObject(json["oneTimePurchaseOfferDetailsAndroid"], "oneTimePurchaseOfferDetailsAndroid")?.let { ProductAndroidOneTimePurchaseOfferDetail.fromJson(it) },
                platform = IapPlatform.fromJson(requireJsonString(json["platform"], "platform")),
                price = optionalJsonDouble(json["price"], "price"),
                subscriptionOfferDetailsAndroid = requireJsonArray(json["subscriptionOfferDetailsAndroid"], "subscriptionOfferDetailsAndroid").map { element -> ProductSubscriptionAndroidOfferDetails.fromJson(requireJsonObject(element, "subscriptionOfferDetailsAndroid[]")) },
                title = requireJsonString(json["title"], "title"),
                type = ProductType.fromJson(requireJsonString(json["type"], "type")),
            )
        }
    }

    override fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "ProductSubscriptionAndroid")
        put("currency", JsonPrimitive(currency))
        put("debugDescription", debugDescription?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("description", JsonPrimitive(description))
        put("displayName", displayName?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("displayPrice", JsonPrimitive(displayPrice))
        put("id", JsonPrimitive(id))
        put("nameAndroid", JsonPrimitive(nameAndroid))
        put("oneTimePurchaseOfferDetailsAndroid", oneTimePurchaseOfferDetailsAndroid?.let { value -> value.toJson() } ?: JsonNull)
        put("platform", JsonPrimitive(platform.toJson()))
        put("price", price?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("subscriptionOfferDetailsAndroid", buildJsonArray { subscriptionOfferDetailsAndroid.forEach { element -> add(element.toJson()) } })
        put("title", JsonPrimitive(title))
        put("type", JsonPrimitive(type.toJson()))
    }
}

public data class ProductSubscriptionAndroidOfferDetails(
    val basePlanId: String,
    val offerId: String? = null,
    val offerTags: List<String>,
    val offerToken: String,
    val pricingPhases: PricingPhasesAndroid
) {

    companion object {
        fun fromJson(json: JsonObject): ProductSubscriptionAndroidOfferDetails {
            return ProductSubscriptionAndroidOfferDetails(
                basePlanId = requireJsonString(json["basePlanId"], "basePlanId"),
                offerId = optionalJsonString(json["offerId"], "offerId"),
                offerTags = requireJsonArray(json["offerTags"], "offerTags").map { element -> requireJsonString(element, "offerTags[]") },
                offerToken = requireJsonString(json["offerToken"], "offerToken"),
                pricingPhases = PricingPhasesAndroid.fromJson(requireJsonObject(json["pricingPhases"], "pricingPhases")),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "ProductSubscriptionAndroidOfferDetails")
        put("basePlanId", JsonPrimitive(basePlanId))
        put("offerId", offerId?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("offerTags", buildJsonArray { offerTags.forEach { element -> add(JsonPrimitive(element)) } })
        put("offerToken", JsonPrimitive(offerToken))
        put("pricingPhases", pricingPhases.toJson())
    }
}

public data class ProductSubscriptionIOS(
    override val currency: String,
    override val debugDescription: String? = null,
    override val description: String,
    val discountsIOS: List<DiscountIOS>? = null,
    override val displayName: String? = null,
    val displayNameIOS: String,
    override val displayPrice: String,
    override val id: String,
    val introductoryPriceAsAmountIOS: String? = null,
    val introductoryPriceIOS: String? = null,
    val introductoryPriceNumberOfPeriodsIOS: String? = null,
    val introductoryPricePaymentModeIOS: PaymentModeIOS? = null,
    val introductoryPriceSubscriptionPeriodIOS: SubscriptionPeriodIOS? = null,
    val isFamilyShareableIOS: Boolean,
    val jsonRepresentationIOS: String,
    override val platform: IapPlatform,
    override val price: Double? = null,
    val subscriptionInfoIOS: SubscriptionInfoIOS? = null,
    val subscriptionPeriodNumberIOS: String? = null,
    val subscriptionPeriodUnitIOS: SubscriptionPeriodIOS? = null,
    override val title: String,
    override val type: ProductType,
    val typeIOS: ProductTypeIOS
) : ProductCommon, ProductSubscription {

    companion object {
        fun fromJson(json: JsonObject): ProductSubscriptionIOS {
            return ProductSubscriptionIOS(
                currency = requireJsonString(json["currency"], "currency"),
                debugDescription = optionalJsonString(json["debugDescription"], "debugDescription"),
                description = requireJsonString(json["description"], "description"),
                discountsIOS = optionalJsonArray(json["discountsIOS"], "discountsIOS")?.map { element -> DiscountIOS.fromJson(requireJsonObject(element, "discountsIOS[]")) },
                displayName = optionalJsonString(json["displayName"], "displayName"),
                displayNameIOS = requireJsonString(json["displayNameIOS"], "displayNameIOS"),
                displayPrice = requireJsonString(json["displayPrice"], "displayPrice"),
                id = requireJsonString(json["id"], "id"),
                introductoryPriceAsAmountIOS = optionalJsonString(json["introductoryPriceAsAmountIOS"], "introductoryPriceAsAmountIOS"),
                introductoryPriceIOS = optionalJsonString(json["introductoryPriceIOS"], "introductoryPriceIOS"),
                introductoryPriceNumberOfPeriodsIOS = optionalJsonString(json["introductoryPriceNumberOfPeriodsIOS"], "introductoryPriceNumberOfPeriodsIOS"),
                introductoryPricePaymentModeIOS = optionalJsonString(json["introductoryPricePaymentModeIOS"], "introductoryPricePaymentModeIOS")?.let { PaymentModeIOS.fromJson(it) },
                introductoryPriceSubscriptionPeriodIOS = optionalJsonString(json["introductoryPriceSubscriptionPeriodIOS"], "introductoryPriceSubscriptionPeriodIOS")?.let { SubscriptionPeriodIOS.fromJson(it) },
                isFamilyShareableIOS = requireJsonBoolean(json["isFamilyShareableIOS"], "isFamilyShareableIOS"),
                jsonRepresentationIOS = requireJsonString(json["jsonRepresentationIOS"], "jsonRepresentationIOS"),
                platform = IapPlatform.fromJson(requireJsonString(json["platform"], "platform")),
                price = optionalJsonDouble(json["price"], "price"),
                subscriptionInfoIOS = optionalJsonObject(json["subscriptionInfoIOS"], "subscriptionInfoIOS")?.let { SubscriptionInfoIOS.fromJson(it) },
                subscriptionPeriodNumberIOS = optionalJsonString(json["subscriptionPeriodNumberIOS"], "subscriptionPeriodNumberIOS"),
                subscriptionPeriodUnitIOS = optionalJsonString(json["subscriptionPeriodUnitIOS"], "subscriptionPeriodUnitIOS")?.let { SubscriptionPeriodIOS.fromJson(it) },
                title = requireJsonString(json["title"], "title"),
                type = ProductType.fromJson(requireJsonString(json["type"], "type")),
                typeIOS = ProductTypeIOS.fromJson(requireJsonString(json["typeIOS"], "typeIOS")),
            )
        }
    }

    override fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "ProductSubscriptionIOS")
        put("currency", JsonPrimitive(currency))
        put("debugDescription", debugDescription?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("description", JsonPrimitive(description))
        put("discountsIOS", discountsIOS?.let { value -> buildJsonArray { value.forEach { element -> add(element.toJson()) } } } ?: JsonNull)
        put("displayName", displayName?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("displayNameIOS", JsonPrimitive(displayNameIOS))
        put("displayPrice", JsonPrimitive(displayPrice))
        put("id", JsonPrimitive(id))
        put("introductoryPriceAsAmountIOS", introductoryPriceAsAmountIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("introductoryPriceIOS", introductoryPriceIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("introductoryPriceNumberOfPeriodsIOS", introductoryPriceNumberOfPeriodsIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("introductoryPricePaymentModeIOS", introductoryPricePaymentModeIOS?.let { value -> JsonPrimitive(value.toJson()) } ?: JsonNull)
        put("introductoryPriceSubscriptionPeriodIOS", introductoryPriceSubscriptionPeriodIOS?.let { value -> JsonPrimitive(value.toJson()) } ?: JsonNull)
        put("isFamilyShareableIOS", JsonPrimitive(isFamilyShareableIOS))
        put("jsonRepresentationIOS", JsonPrimitive(jsonRepresentationIOS))
        put("platform", JsonPrimitive(platform.toJson()))
        put("price", price?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("subscriptionInfoIOS", subscriptionInfoIOS?.let { value -> value.toJson() } ?: JsonNull)
        put("subscriptionPeriodNumberIOS", subscriptionPeriodNumberIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("subscriptionPeriodUnitIOS", subscriptionPeriodUnitIOS?.let { value -> JsonPrimitive(value.toJson()) } ?: JsonNull)
        put("title", JsonPrimitive(title))
        put("type", JsonPrimitive(type.toJson()))
        put("typeIOS", JsonPrimitive(typeIOS.toJson()))
    }
}

public data class PurchaseAndroid(
    val autoRenewingAndroid: Boolean? = null,
    val dataAndroid: String? = null,
    val developerPayloadAndroid: String? = null,
    override val id: String,
    override val ids: List<String>? = null,
    val isAcknowledgedAndroid: Boolean? = null,
    override val isAutoRenewing: Boolean,
    val obfuscatedAccountIdAndroid: String? = null,
    val obfuscatedProfileIdAndroid: String? = null,
    val packageNameAndroid: String? = null,
    override val platform: IapPlatform,
    override val productId: String,
    override val purchaseState: PurchaseState,
    override val purchaseToken: String? = null,
    override val quantity: Int,
    val signatureAndroid: String? = null,
    override val transactionDate: Double,
    val transactionId: String? = null
) : PurchaseCommon, Purchase {

    companion object {
        fun fromJson(json: JsonObject): PurchaseAndroid {
            return PurchaseAndroid(
                autoRenewingAndroid = optionalJsonBoolean(json["autoRenewingAndroid"], "autoRenewingAndroid"),
                dataAndroid = optionalJsonString(json["dataAndroid"], "dataAndroid"),
                developerPayloadAndroid = optionalJsonString(json["developerPayloadAndroid"], "developerPayloadAndroid"),
                id = requireJsonString(json["id"], "id"),
                ids = optionalJsonArray(json["ids"], "ids")?.map { element -> requireJsonString(element, "ids[]") },
                isAcknowledgedAndroid = optionalJsonBoolean(json["isAcknowledgedAndroid"], "isAcknowledgedAndroid"),
                isAutoRenewing = requireJsonBoolean(json["isAutoRenewing"], "isAutoRenewing"),
                obfuscatedAccountIdAndroid = optionalJsonString(json["obfuscatedAccountIdAndroid"], "obfuscatedAccountIdAndroid"),
                obfuscatedProfileIdAndroid = optionalJsonString(json["obfuscatedProfileIdAndroid"], "obfuscatedProfileIdAndroid"),
                packageNameAndroid = optionalJsonString(json["packageNameAndroid"], "packageNameAndroid"),
                platform = IapPlatform.fromJson(requireJsonString(json["platform"], "platform")),
                productId = requireJsonString(json["productId"], "productId"),
                purchaseState = PurchaseState.fromJson(requireJsonString(json["purchaseState"], "purchaseState")),
                purchaseToken = optionalJsonString(json["purchaseToken"], "purchaseToken"),
                quantity = requireJsonInt(json["quantity"], "quantity"),
                signatureAndroid = optionalJsonString(json["signatureAndroid"], "signatureAndroid"),
                transactionDate = requireJsonDouble(json["transactionDate"], "transactionDate"),
                transactionId = optionalJsonString(json["transactionId"], "transactionId"),
            )
        }
    }

    override fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "PurchaseAndroid")
        put("autoRenewingAndroid", autoRenewingAndroid?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("dataAndroid", dataAndroid?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("developerPayloadAndroid", developerPayloadAndroid?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("id", JsonPrimitive(id))
        put("ids", ids?.let { value -> buildJsonArray { value.forEach { element -> add(JsonPrimitive(element)) } } } ?: JsonNull)
        put("isAcknowledgedAndroid", isAcknowledgedAndroid?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("isAutoRenewing", JsonPrimitive(isAutoRenewing))
        put("obfuscatedAccountIdAndroid", obfuscatedAccountIdAndroid?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("obfuscatedProfileIdAndroid", obfuscatedProfileIdAndroid?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("packageNameAndroid", packageNameAndroid?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("platform", JsonPrimitive(platform.toJson()))
        put("productId", JsonPrimitive(productId))
        put("purchaseState", JsonPrimitive(purchaseState.toJson()))
        put("purchaseToken", purchaseToken?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("quantity", JsonPrimitive(quantity))
        put("signatureAndroid", signatureAndroid?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("transactionDate", JsonPrimitive(transactionDate))
        put("transactionId", transactionId?.let { value -> JsonPrimitive(value) } ?: JsonNull)
    }
}

public data class PurchaseError(
    val code: ErrorCode,
    val message: String,
    val productId: String? = null
) {

    companion object {
        fun fromJson(json: JsonObject): PurchaseError {
            return PurchaseError(
                code = ErrorCode.fromJson(requireJsonString(json["code"], "code")),
                message = requireJsonString(json["message"], "message"),
                productId = optionalJsonString(json["productId"], "productId"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "PurchaseError")
        put("code", JsonPrimitive(code.toJson()))
        put("message", JsonPrimitive(message))
        put("productId", productId?.let { value -> JsonPrimitive(value) } ?: JsonNull)
    }
}

public data class PurchaseIOS(
    val appAccountToken: String? = null,
    val appBundleIdIOS: String? = null,
    val countryCodeIOS: String? = null,
    val currencyCodeIOS: String? = null,
    val currencySymbolIOS: String? = null,
    val environmentIOS: String? = null,
    val expirationDateIOS: Double? = null,
    override val id: String,
    override val ids: List<String>? = null,
    override val isAutoRenewing: Boolean,
    val isUpgradedIOS: Boolean? = null,
    val offerIOS: PurchaseOfferIOS? = null,
    val originalTransactionDateIOS: Double? = null,
    val originalTransactionIdentifierIOS: String? = null,
    val ownershipTypeIOS: String? = null,
    override val platform: IapPlatform,
    override val productId: String,
    override val purchaseState: PurchaseState,
    override val purchaseToken: String? = null,
    override val quantity: Int,
    val quantityIOS: Int? = null,
    val reasonIOS: String? = null,
    val reasonStringRepresentationIOS: String? = null,
    val revocationDateIOS: Double? = null,
    val revocationReasonIOS: String? = null,
    val storefrontCountryCodeIOS: String? = null,
    val subscriptionGroupIdIOS: String? = null,
    override val transactionDate: Double,
    val transactionId: String,
    val transactionReasonIOS: String? = null,
    val webOrderLineItemIdIOS: String? = null
) : PurchaseCommon, Purchase {

    companion object {
        fun fromJson(json: JsonObject): PurchaseIOS {
            return PurchaseIOS(
                appAccountToken = optionalJsonString(json["appAccountToken"], "appAccountToken"),
                appBundleIdIOS = optionalJsonString(json["appBundleIdIOS"], "appBundleIdIOS"),
                countryCodeIOS = optionalJsonString(json["countryCodeIOS"], "countryCodeIOS"),
                currencyCodeIOS = optionalJsonString(json["currencyCodeIOS"], "currencyCodeIOS"),
                currencySymbolIOS = optionalJsonString(json["currencySymbolIOS"], "currencySymbolIOS"),
                environmentIOS = optionalJsonString(json["environmentIOS"], "environmentIOS"),
                expirationDateIOS = optionalJsonDouble(json["expirationDateIOS"], "expirationDateIOS"),
                id = requireJsonString(json["id"], "id"),
                ids = optionalJsonArray(json["ids"], "ids")?.map { element -> requireJsonString(element, "ids[]") },
                isAutoRenewing = requireJsonBoolean(json["isAutoRenewing"], "isAutoRenewing"),
                isUpgradedIOS = optionalJsonBoolean(json["isUpgradedIOS"], "isUpgradedIOS"),
                offerIOS = optionalJsonObject(json["offerIOS"], "offerIOS")?.let { PurchaseOfferIOS.fromJson(it) },
                originalTransactionDateIOS = optionalJsonDouble(json["originalTransactionDateIOS"], "originalTransactionDateIOS"),
                originalTransactionIdentifierIOS = optionalJsonString(json["originalTransactionIdentifierIOS"], "originalTransactionIdentifierIOS"),
                ownershipTypeIOS = optionalJsonString(json["ownershipTypeIOS"], "ownershipTypeIOS"),
                platform = IapPlatform.fromJson(requireJsonString(json["platform"], "platform")),
                productId = requireJsonString(json["productId"], "productId"),
                purchaseState = PurchaseState.fromJson(requireJsonString(json["purchaseState"], "purchaseState")),
                purchaseToken = optionalJsonString(json["purchaseToken"], "purchaseToken"),
                quantity = requireJsonInt(json["quantity"], "quantity"),
                quantityIOS = optionalJsonInt(json["quantityIOS"], "quantityIOS"),
                reasonIOS = optionalJsonString(json["reasonIOS"], "reasonIOS"),
                reasonStringRepresentationIOS = optionalJsonString(json["reasonStringRepresentationIOS"], "reasonStringRepresentationIOS"),
                revocationDateIOS = optionalJsonDouble(json["revocationDateIOS"], "revocationDateIOS"),
                revocationReasonIOS = optionalJsonString(json["revocationReasonIOS"], "revocationReasonIOS"),
                storefrontCountryCodeIOS = optionalJsonString(json["storefrontCountryCodeIOS"], "storefrontCountryCodeIOS"),
                subscriptionGroupIdIOS = optionalJsonString(json["subscriptionGroupIdIOS"], "subscriptionGroupIdIOS"),
                transactionDate = requireJsonDouble(json["transactionDate"], "transactionDate"),
                transactionId = requireJsonString(json["transactionId"], "transactionId"),
                transactionReasonIOS = optionalJsonString(json["transactionReasonIOS"], "transactionReasonIOS"),
                webOrderLineItemIdIOS = optionalJsonString(json["webOrderLineItemIdIOS"], "webOrderLineItemIdIOS"),
            )
        }
    }

    override fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "PurchaseIOS")
        put("appAccountToken", appAccountToken?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("appBundleIdIOS", appBundleIdIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("countryCodeIOS", countryCodeIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("currencyCodeIOS", currencyCodeIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("currencySymbolIOS", currencySymbolIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("environmentIOS", environmentIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("expirationDateIOS", expirationDateIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("id", JsonPrimitive(id))
        put("ids", ids?.let { value -> buildJsonArray { value.forEach { element -> add(JsonPrimitive(element)) } } } ?: JsonNull)
        put("isAutoRenewing", JsonPrimitive(isAutoRenewing))
        put("isUpgradedIOS", isUpgradedIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("offerIOS", offerIOS?.let { value -> value.toJson() } ?: JsonNull)
        put("originalTransactionDateIOS", originalTransactionDateIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("originalTransactionIdentifierIOS", originalTransactionIdentifierIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("ownershipTypeIOS", ownershipTypeIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("platform", JsonPrimitive(platform.toJson()))
        put("productId", JsonPrimitive(productId))
        put("purchaseState", JsonPrimitive(purchaseState.toJson()))
        put("purchaseToken", purchaseToken?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("quantity", JsonPrimitive(quantity))
        put("quantityIOS", quantityIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("reasonIOS", reasonIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("reasonStringRepresentationIOS", reasonStringRepresentationIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("revocationDateIOS", revocationDateIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("revocationReasonIOS", revocationReasonIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("storefrontCountryCodeIOS", storefrontCountryCodeIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("subscriptionGroupIdIOS", subscriptionGroupIdIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("transactionDate", JsonPrimitive(transactionDate))
        put("transactionId", JsonPrimitive(transactionId))
        put("transactionReasonIOS", transactionReasonIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("webOrderLineItemIdIOS", webOrderLineItemIdIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
    }
}

public data class PurchaseOfferIOS(
    val id: String,
    val paymentMode: String,
    val type: String
) {

    companion object {
        fun fromJson(json: JsonObject): PurchaseOfferIOS {
            return PurchaseOfferIOS(
                id = requireJsonString(json["id"], "id"),
                paymentMode = requireJsonString(json["paymentMode"], "paymentMode"),
                type = requireJsonString(json["type"], "type"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "PurchaseOfferIOS")
        put("id", JsonPrimitive(id))
        put("paymentMode", JsonPrimitive(paymentMode))
        put("type", JsonPrimitive(type))
    }
}

public data class ReceiptValidationResultAndroid(
    val autoRenewing: Boolean,
    val betaProduct: Boolean,
    val cancelDate: Double? = null,
    val cancelReason: String? = null,
    val deferredDate: Double? = null,
    val deferredSku: String? = null,
    val freeTrialEndDate: Double,
    val gracePeriodEndDate: Double,
    val parentProductId: String,
    val productId: String,
    val productType: String,
    val purchaseDate: Double,
    val quantity: Int,
    val receiptId: String,
    val renewalDate: Double,
    val term: String,
    val termSku: String,
    val testTransaction: Boolean
) : ReceiptValidationResult {

    companion object {
        fun fromJson(json: JsonObject): ReceiptValidationResultAndroid {
            return ReceiptValidationResultAndroid(
                autoRenewing = requireJsonBoolean(json["autoRenewing"], "autoRenewing"),
                betaProduct = requireJsonBoolean(json["betaProduct"], "betaProduct"),
                cancelDate = optionalJsonDouble(json["cancelDate"], "cancelDate"),
                cancelReason = optionalJsonString(json["cancelReason"], "cancelReason"),
                deferredDate = optionalJsonDouble(json["deferredDate"], "deferredDate"),
                deferredSku = optionalJsonString(json["deferredSku"], "deferredSku"),
                freeTrialEndDate = requireJsonDouble(json["freeTrialEndDate"], "freeTrialEndDate"),
                gracePeriodEndDate = requireJsonDouble(json["gracePeriodEndDate"], "gracePeriodEndDate"),
                parentProductId = requireJsonString(json["parentProductId"], "parentProductId"),
                productId = requireJsonString(json["productId"], "productId"),
                productType = requireJsonString(json["productType"], "productType"),
                purchaseDate = requireJsonDouble(json["purchaseDate"], "purchaseDate"),
                quantity = requireJsonInt(json["quantity"], "quantity"),
                receiptId = requireJsonString(json["receiptId"], "receiptId"),
                renewalDate = requireJsonDouble(json["renewalDate"], "renewalDate"),
                term = requireJsonString(json["term"], "term"),
                termSku = requireJsonString(json["termSku"], "termSku"),
                testTransaction = requireJsonBoolean(json["testTransaction"], "testTransaction"),
            )
        }
    }

    override fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "ReceiptValidationResultAndroid")
        put("autoRenewing", JsonPrimitive(autoRenewing))
        put("betaProduct", JsonPrimitive(betaProduct))
        put("cancelDate", cancelDate?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("cancelReason", cancelReason?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("deferredDate", deferredDate?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("deferredSku", deferredSku?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("freeTrialEndDate", JsonPrimitive(freeTrialEndDate))
        put("gracePeriodEndDate", JsonPrimitive(gracePeriodEndDate))
        put("parentProductId", JsonPrimitive(parentProductId))
        put("productId", JsonPrimitive(productId))
        put("productType", JsonPrimitive(productType))
        put("purchaseDate", JsonPrimitive(purchaseDate))
        put("quantity", JsonPrimitive(quantity))
        put("receiptId", JsonPrimitive(receiptId))
        put("renewalDate", JsonPrimitive(renewalDate))
        put("term", JsonPrimitive(term))
        put("termSku", JsonPrimitive(termSku))
        put("testTransaction", JsonPrimitive(testTransaction))
    }
}

public data class ReceiptValidationResultIOS(
    /**
     * Whether the receipt is valid
     */
    val isValid: Boolean,
    /**
     * JWS representation
     */
    val jwsRepresentation: String,
    /**
     * Latest transaction if available
     */
    val latestTransaction: Purchase? = null,
    /**
     * Receipt data string
     */
    val receiptData: String
) : ReceiptValidationResult {

    companion object {
        fun fromJson(json: JsonObject): ReceiptValidationResultIOS {
            return ReceiptValidationResultIOS(
                isValid = requireJsonBoolean(json["isValid"], "isValid"),
                jwsRepresentation = requireJsonString(json["jwsRepresentation"], "jwsRepresentation"),
                latestTransaction = optionalJsonObject(json["latestTransaction"], "latestTransaction")?.let { Purchase.fromJson(it) },
                receiptData = requireJsonString(json["receiptData"], "receiptData"),
            )
        }
    }

    override fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "ReceiptValidationResultIOS")
        put("isValid", JsonPrimitive(isValid))
        put("jwsRepresentation", JsonPrimitive(jwsRepresentation))
        put("latestTransaction", latestTransaction?.let { value -> value.toJson() } ?: JsonNull)
        put("receiptData", JsonPrimitive(receiptData))
    }
}

public data class RefundResultIOS(
    val message: String? = null,
    val status: String
) {

    companion object {
        fun fromJson(json: JsonObject): RefundResultIOS {
            return RefundResultIOS(
                message = optionalJsonString(json["message"], "message"),
                status = requireJsonString(json["status"], "status"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "RefundResultIOS")
        put("message", message?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("status", JsonPrimitive(status))
    }
}

public data class RenewalInfoIOS(
    val autoRenewPreference: String? = null,
    val jsonRepresentation: String? = null,
    val willAutoRenew: Boolean
) {

    companion object {
        fun fromJson(json: JsonObject): RenewalInfoIOS {
            return RenewalInfoIOS(
                autoRenewPreference = optionalJsonString(json["autoRenewPreference"], "autoRenewPreference"),
                jsonRepresentation = optionalJsonString(json["jsonRepresentation"], "jsonRepresentation"),
                willAutoRenew = requireJsonBoolean(json["willAutoRenew"], "willAutoRenew"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "RenewalInfoIOS")
        put("autoRenewPreference", autoRenewPreference?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("jsonRepresentation", jsonRepresentation?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("willAutoRenew", JsonPrimitive(willAutoRenew))
    }
}

public sealed interface RequestPurchaseResult

public data class RequestPurchaseResultPurchase(val value: Purchase?) : RequestPurchaseResult

public data class RequestPurchaseResultPurchases(val value: List<Purchase>?) : RequestPurchaseResult

public data class SubscriptionInfoIOS(
    val introductoryOffer: SubscriptionOfferIOS? = null,
    val promotionalOffers: List<SubscriptionOfferIOS>? = null,
    val subscriptionGroupId: String,
    val subscriptionPeriod: SubscriptionPeriodValueIOS
) {

    companion object {
        fun fromJson(json: JsonObject): SubscriptionInfoIOS {
            return SubscriptionInfoIOS(
                introductoryOffer = optionalJsonObject(json["introductoryOffer"], "introductoryOffer")?.let { SubscriptionOfferIOS.fromJson(it) },
                promotionalOffers = optionalJsonArray(json["promotionalOffers"], "promotionalOffers")?.map { element -> SubscriptionOfferIOS.fromJson(requireJsonObject(element, "promotionalOffers[]")) },
                subscriptionGroupId = requireJsonString(json["subscriptionGroupId"], "subscriptionGroupId"),
                subscriptionPeriod = SubscriptionPeriodValueIOS.fromJson(requireJsonObject(json["subscriptionPeriod"], "subscriptionPeriod")),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "SubscriptionInfoIOS")
        put("introductoryOffer", introductoryOffer?.let { value -> value.toJson() } ?: JsonNull)
        put("promotionalOffers", promotionalOffers?.let { value -> buildJsonArray { value.forEach { element -> add(element.toJson()) } } } ?: JsonNull)
        put("subscriptionGroupId", JsonPrimitive(subscriptionGroupId))
        put("subscriptionPeriod", subscriptionPeriod.toJson())
    }
}

public data class SubscriptionOfferIOS(
    val displayPrice: String,
    val id: String,
    val paymentMode: PaymentModeIOS,
    val period: SubscriptionPeriodValueIOS,
    val periodCount: Int,
    val price: Double,
    val type: SubscriptionOfferTypeIOS
) {

    companion object {
        fun fromJson(json: JsonObject): SubscriptionOfferIOS {
            return SubscriptionOfferIOS(
                displayPrice = requireJsonString(json["displayPrice"], "displayPrice"),
                id = requireJsonString(json["id"], "id"),
                paymentMode = PaymentModeIOS.fromJson(requireJsonString(json["paymentMode"], "paymentMode")),
                period = SubscriptionPeriodValueIOS.fromJson(requireJsonObject(json["period"], "period")),
                periodCount = requireJsonInt(json["periodCount"], "periodCount"),
                price = requireJsonDouble(json["price"], "price"),
                type = SubscriptionOfferTypeIOS.fromJson(requireJsonString(json["type"], "type")),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "SubscriptionOfferIOS")
        put("displayPrice", JsonPrimitive(displayPrice))
        put("id", JsonPrimitive(id))
        put("paymentMode", JsonPrimitive(paymentMode.toJson()))
        put("period", period.toJson())
        put("periodCount", JsonPrimitive(periodCount))
        put("price", JsonPrimitive(price))
        put("type", JsonPrimitive(type.toJson()))
    }
}

public data class SubscriptionPeriodValueIOS(
    val unit: SubscriptionPeriodIOS,
    val value: Int
) {

    companion object {
        fun fromJson(json: JsonObject): SubscriptionPeriodValueIOS {
            return SubscriptionPeriodValueIOS(
                unit = SubscriptionPeriodIOS.fromJson(requireJsonString(json["unit"], "unit")),
                value = requireJsonInt(json["value"], "value"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "SubscriptionPeriodValueIOS")
        put("unit", JsonPrimitive(unit.toJson()))
        put("value", JsonPrimitive(value))
    }
}

public data class SubscriptionStatusIOS(
    val renewalInfo: RenewalInfoIOS? = null,
    val state: String
) {

    companion object {
        fun fromJson(json: JsonObject): SubscriptionStatusIOS {
            return SubscriptionStatusIOS(
                renewalInfo = optionalJsonObject(json["renewalInfo"], "renewalInfo")?.let { RenewalInfoIOS.fromJson(it) },
                state = requireJsonString(json["state"], "state"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("__typename", "SubscriptionStatusIOS")
        put("renewalInfo", renewalInfo?.let { value -> value.toJson() } ?: JsonNull)
        put("state", JsonPrimitive(state))
    }
}

public typealias VoidResult = Unit

// MARK: - Input Objects

public data class AndroidSubscriptionOfferInput(
    /**
     * Offer token
     */
    val offerToken: String,
    /**
     * Product SKU
     */
    val sku: String
) {
    companion object {
        fun fromJson(json: JsonObject): AndroidSubscriptionOfferInput {
            return AndroidSubscriptionOfferInput(
                offerToken = requireJsonString(json["offerToken"], "offerToken"),
                sku = requireJsonString(json["sku"], "sku"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("offerToken", JsonPrimitive(offerToken))
        put("sku", JsonPrimitive(sku))
    }
}

public data class DeepLinkOptions(
    /**
     * Android package name to target (required on Android)
     */
    val packageNameAndroid: String? = null,
    /**
     * Android SKU to open (required on Android)
     */
    val skuAndroid: String? = null
) {
    companion object {
        fun fromJson(json: JsonObject): DeepLinkOptions {
            return DeepLinkOptions(
                packageNameAndroid = optionalJsonString(json["packageNameAndroid"], "packageNameAndroid"),
                skuAndroid = optionalJsonString(json["skuAndroid"], "skuAndroid"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("packageNameAndroid", packageNameAndroid?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("skuAndroid", skuAndroid?.let { value -> JsonPrimitive(value) } ?: JsonNull)
    }
}

public data class DiscountOfferInputIOS(
    /**
     * Discount identifier
     */
    val identifier: String,
    /**
     * Key identifier for validation
     */
    val keyIdentifier: String,
    /**
     * Cryptographic nonce
     */
    val nonce: String,
    /**
     * Signature for validation
     */
    val signature: String,
    /**
     * Timestamp of discount offer
     */
    val timestamp: Double
) {
    companion object {
        fun fromJson(json: JsonObject): DiscountOfferInputIOS {
            return DiscountOfferInputIOS(
                identifier = requireJsonString(json["identifier"], "identifier"),
                keyIdentifier = requireJsonString(json["keyIdentifier"], "keyIdentifier"),
                nonce = requireJsonString(json["nonce"], "nonce"),
                signature = requireJsonString(json["signature"], "signature"),
                timestamp = requireJsonDouble(json["timestamp"], "timestamp"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("identifier", JsonPrimitive(identifier))
        put("keyIdentifier", JsonPrimitive(keyIdentifier))
        put("nonce", JsonPrimitive(nonce))
        put("signature", JsonPrimitive(signature))
        put("timestamp", JsonPrimitive(timestamp))
    }
}

public data class ProductRequest(
    val skus: List<String>,
    val type: ProductQueryType? = null
) {
    companion object {
        fun fromJson(json: JsonObject): ProductRequest {
            return ProductRequest(
                skus = requireJsonArray(json["skus"], "skus").map { element -> requireJsonString(element, "skus[]") },
                type = optionalJsonString(json["type"], "type")?.let { ProductQueryType.fromJson(it) },
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("skus", buildJsonArray { skus.forEach { element -> add(JsonPrimitive(element)) } })
        put("type", type?.let { value -> JsonPrimitive(value.toJson()) } ?: JsonNull)
    }
}

public data class PurchaseInput(
    val id: String,
    val ids: List<String>? = null,
    val isAutoRenewing: Boolean,
    val platform: IapPlatform,
    val productId: String,
    val purchaseState: PurchaseState,
    val purchaseToken: String? = null,
    val quantity: Int,
    val transactionDate: Double
) {
    companion object {
        fun fromJson(json: JsonObject): PurchaseInput {
            return PurchaseInput(
                id = requireJsonString(json["id"], "id"),
                ids = optionalJsonArray(json["ids"], "ids")?.map { element -> requireJsonString(element, "ids[]") },
                isAutoRenewing = requireJsonBoolean(json["isAutoRenewing"], "isAutoRenewing"),
                platform = IapPlatform.fromJson(requireJsonString(json["platform"], "platform")),
                productId = requireJsonString(json["productId"], "productId"),
                purchaseState = PurchaseState.fromJson(requireJsonString(json["purchaseState"], "purchaseState")),
                purchaseToken = optionalJsonString(json["purchaseToken"], "purchaseToken"),
                quantity = requireJsonInt(json["quantity"], "quantity"),
                transactionDate = requireJsonDouble(json["transactionDate"], "transactionDate"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("id", JsonPrimitive(id))
        put("ids", ids?.let { value -> buildJsonArray { value.forEach { element -> add(JsonPrimitive(element)) } } } ?: JsonNull)
        put("isAutoRenewing", JsonPrimitive(isAutoRenewing))
        put("platform", JsonPrimitive(platform.toJson()))
        put("productId", JsonPrimitive(productId))
        put("purchaseState", JsonPrimitive(purchaseState.toJson()))
        put("purchaseToken", purchaseToken?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("quantity", JsonPrimitive(quantity))
        put("transactionDate", JsonPrimitive(transactionDate))
    }
}

public data class PurchaseOptions(
    /**
     * Also emit results through the iOS event listeners
     */
    val alsoPublishToEventListenerIOS: Boolean? = null,
    /**
     * Limit to currently active items on iOS
     */
    val onlyIncludeActiveItemsIOS: Boolean? = null
) {
    companion object {
        fun fromJson(json: JsonObject): PurchaseOptions {
            return PurchaseOptions(
                alsoPublishToEventListenerIOS = optionalJsonBoolean(json["alsoPublishToEventListenerIOS"], "alsoPublishToEventListenerIOS"),
                onlyIncludeActiveItemsIOS = optionalJsonBoolean(json["onlyIncludeActiveItemsIOS"], "onlyIncludeActiveItemsIOS"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("alsoPublishToEventListenerIOS", alsoPublishToEventListenerIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("onlyIncludeActiveItemsIOS", onlyIncludeActiveItemsIOS?.let { value -> JsonPrimitive(value) } ?: JsonNull)
    }
}

public data class ReceiptValidationAndroidOptions(
    val accessToken: String,
    val isSub: Boolean? = null,
    val packageName: String,
    val productToken: String
) {
    companion object {
        fun fromJson(json: JsonObject): ReceiptValidationAndroidOptions {
            return ReceiptValidationAndroidOptions(
                accessToken = requireJsonString(json["accessToken"], "accessToken"),
                isSub = optionalJsonBoolean(json["isSub"], "isSub"),
                packageName = requireJsonString(json["packageName"], "packageName"),
                productToken = requireJsonString(json["productToken"], "productToken"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("accessToken", JsonPrimitive(accessToken))
        put("isSub", isSub?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("packageName", JsonPrimitive(packageName))
        put("productToken", JsonPrimitive(productToken))
    }
}

public data class ReceiptValidationProps(
    /**
     * Android-specific validation options
     */
    val androidOptions: ReceiptValidationAndroidOptions? = null,
    /**
     * Product SKU to validate
     */
    val sku: String
) {
    companion object {
        fun fromJson(json: JsonObject): ReceiptValidationProps {
            return ReceiptValidationProps(
                androidOptions = optionalJsonObject(json["androidOptions"], "androidOptions")?.let { ReceiptValidationAndroidOptions.fromJson(it) },
                sku = requireJsonString(json["sku"], "sku"),
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("androidOptions", androidOptions?.let { value -> value.toJson() } ?: JsonNull)
        put("sku", JsonPrimitive(sku))
    }
}

public data class RequestPurchaseAndroidProps(
    /**
     * Personalized offer flag
     */
    val isOfferPersonalized: Boolean? = null,
    /**
     * Obfuscated account ID
     */
    val obfuscatedAccountIdAndroid: String? = null,
    /**
     * Obfuscated profile ID
     */
    val obfuscatedProfileIdAndroid: String? = null,
    /**
     * List of product SKUs
     */
    val skus: List<String>
) {
    companion object {
        fun fromJson(json: JsonObject): RequestPurchaseAndroidProps {
            return RequestPurchaseAndroidProps(
                isOfferPersonalized = optionalJsonBoolean(json["isOfferPersonalized"], "isOfferPersonalized"),
                obfuscatedAccountIdAndroid = optionalJsonString(json["obfuscatedAccountIdAndroid"], "obfuscatedAccountIdAndroid"),
                obfuscatedProfileIdAndroid = optionalJsonString(json["obfuscatedProfileIdAndroid"], "obfuscatedProfileIdAndroid"),
                skus = requireJsonArray(json["skus"], "skus").map { element -> requireJsonString(element, "skus[]") },
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("isOfferPersonalized", isOfferPersonalized?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("obfuscatedAccountIdAndroid", obfuscatedAccountIdAndroid?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("obfuscatedProfileIdAndroid", obfuscatedProfileIdAndroid?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("skus", buildJsonArray { skus.forEach { element -> add(JsonPrimitive(element)) } })
    }
}

public data class RequestPurchaseIosProps(
    /**
     * Auto-finish transaction (dangerous)
     */
    val andDangerouslyFinishTransactionAutomatically: Boolean? = null,
    /**
     * App account token for user tracking
     */
    val appAccountToken: String? = null,
    /**
     * Purchase quantity
     */
    val quantity: Int? = null,
    /**
     * Product SKU
     */
    val sku: String,
    /**
     * Discount offer to apply
     */
    val withOffer: DiscountOfferInputIOS? = null
) {
    companion object {
        fun fromJson(json: JsonObject): RequestPurchaseIosProps {
            return RequestPurchaseIosProps(
                andDangerouslyFinishTransactionAutomatically = optionalJsonBoolean(json["andDangerouslyFinishTransactionAutomatically"], "andDangerouslyFinishTransactionAutomatically"),
                appAccountToken = optionalJsonString(json["appAccountToken"], "appAccountToken"),
                quantity = optionalJsonInt(json["quantity"], "quantity"),
                sku = requireJsonString(json["sku"], "sku"),
                withOffer = optionalJsonObject(json["withOffer"], "withOffer")?.let { DiscountOfferInputIOS.fromJson(it) },
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("andDangerouslyFinishTransactionAutomatically", andDangerouslyFinishTransactionAutomatically?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("appAccountToken", appAccountToken?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("quantity", quantity?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("sku", JsonPrimitive(sku))
        put("withOffer", withOffer?.let { value -> value.toJson() } ?: JsonNull)
    }
}

public data class RequestPurchaseProps(
    val request: Request,
    val type: ProductQueryType
) {
    init {
        when (request) {
            is Request.Purchase -> require(type == ProductQueryType.InApp) { "type must be IN_APP when request is purchase" }
            is Request.Subscription -> require(type == ProductQueryType.Subs) { "type must be SUBS when request is subscription" }
        }
    }

    companion object {
        fun fromJson(json: JsonObject): RequestPurchaseProps {
            val rawType = optionalJsonString(json["type"], "type")?.let { ProductQueryType.fromJson(it) }
            val purchaseJson = optionalJsonObject(json["requestPurchase"], "requestPurchase")
            if (purchaseJson != null) {
                val request = Request.Purchase(RequestPurchasePropsByPlatforms.fromJson(purchaseJson))
                val finalType = rawType ?: ProductQueryType.InApp
                require(finalType == ProductQueryType.InApp) { "type must be IN_APP when requestPurchase is provided" }
                return RequestPurchaseProps(request = request, type = finalType)
            }
            val subscriptionJson = optionalJsonObject(json["requestSubscription"], "requestSubscription")
            if (subscriptionJson != null) {
                val request = Request.Subscription(RequestSubscriptionPropsByPlatforms.fromJson(subscriptionJson))
                val finalType = rawType ?: ProductQueryType.Subs
                require(finalType == ProductQueryType.Subs) { "type must be SUBS when requestSubscription is provided" }
                return RequestPurchaseProps(request = request, type = finalType)
            }
            throw IllegalArgumentException("RequestPurchaseProps requires requestPurchase or requestSubscription")
        }
    }

    fun toJson(): JsonObject = when (request) {
        is Request.Purchase -> buildJsonObject {
            put("requestPurchase", request.value.toJson())
            put("type", JsonPrimitive(type.toJson()))
        }
        is Request.Subscription -> buildJsonObject {
            put("requestSubscription", request.value.toJson())
            put("type", JsonPrimitive(type.toJson()))
        }
    }

    sealed class Request {
        data class Purchase(val value: RequestPurchasePropsByPlatforms) : Request()
        data class Subscription(val value: RequestSubscriptionPropsByPlatforms) : Request()
    }
}

public data class RequestPurchasePropsByPlatforms(
    /**
     * Android-specific purchase parameters
     */
    val android: RequestPurchaseAndroidProps? = null,
    /**
     * iOS-specific purchase parameters
     */
    val ios: RequestPurchaseIosProps? = null
) {
    companion object {
        fun fromJson(json: JsonObject): RequestPurchasePropsByPlatforms {
            return RequestPurchasePropsByPlatforms(
                android = optionalJsonObject(json["android"], "android")?.let { RequestPurchaseAndroidProps.fromJson(it) },
                ios = optionalJsonObject(json["ios"], "ios")?.let { RequestPurchaseIosProps.fromJson(it) },
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("android", android?.let { value -> value.toJson() } ?: JsonNull)
        put("ios", ios?.let { value -> value.toJson() } ?: JsonNull)
    }
}

public data class RequestSubscriptionAndroidProps(
    /**
     * Personalized offer flag
     */
    val isOfferPersonalized: Boolean? = null,
    /**
     * Obfuscated account ID
     */
    val obfuscatedAccountIdAndroid: String? = null,
    /**
     * Obfuscated profile ID
     */
    val obfuscatedProfileIdAndroid: String? = null,
    /**
     * Purchase token for upgrades/downgrades
     */
    val purchaseTokenAndroid: String? = null,
    /**
     * Replacement mode for subscription changes
     */
    val replacementModeAndroid: Int? = null,
    /**
     * List of subscription SKUs
     */
    val skus: List<String>,
    /**
     * Subscription offers
     */
    val subscriptionOffers: List<AndroidSubscriptionOfferInput>? = null
) {
    companion object {
        fun fromJson(json: JsonObject): RequestSubscriptionAndroidProps {
            return RequestSubscriptionAndroidProps(
                isOfferPersonalized = optionalJsonBoolean(json["isOfferPersonalized"], "isOfferPersonalized"),
                obfuscatedAccountIdAndroid = optionalJsonString(json["obfuscatedAccountIdAndroid"], "obfuscatedAccountIdAndroid"),
                obfuscatedProfileIdAndroid = optionalJsonString(json["obfuscatedProfileIdAndroid"], "obfuscatedProfileIdAndroid"),
                purchaseTokenAndroid = optionalJsonString(json["purchaseTokenAndroid"], "purchaseTokenAndroid"),
                replacementModeAndroid = optionalJsonInt(json["replacementModeAndroid"], "replacementModeAndroid"),
                skus = requireJsonArray(json["skus"], "skus").map { element -> requireJsonString(element, "skus[]") },
                subscriptionOffers = optionalJsonArray(json["subscriptionOffers"], "subscriptionOffers")?.map { element -> AndroidSubscriptionOfferInput.fromJson(requireJsonObject(element, "subscriptionOffers[]")) },
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("isOfferPersonalized", isOfferPersonalized?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("obfuscatedAccountIdAndroid", obfuscatedAccountIdAndroid?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("obfuscatedProfileIdAndroid", obfuscatedProfileIdAndroid?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("purchaseTokenAndroid", purchaseTokenAndroid?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("replacementModeAndroid", replacementModeAndroid?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("skus", buildJsonArray { skus.forEach { element -> add(JsonPrimitive(element)) } })
        put("subscriptionOffers", subscriptionOffers?.let { value -> buildJsonArray { value.forEach { element -> add(element.toJson()) } } } ?: JsonNull)
    }
}

public data class RequestSubscriptionIosProps(
    val andDangerouslyFinishTransactionAutomatically: Boolean? = null,
    val appAccountToken: String? = null,
    val quantity: Int? = null,
    val sku: String,
    val withOffer: DiscountOfferInputIOS? = null
) {
    companion object {
        fun fromJson(json: JsonObject): RequestSubscriptionIosProps {
            return RequestSubscriptionIosProps(
                andDangerouslyFinishTransactionAutomatically = optionalJsonBoolean(json["andDangerouslyFinishTransactionAutomatically"], "andDangerouslyFinishTransactionAutomatically"),
                appAccountToken = optionalJsonString(json["appAccountToken"], "appAccountToken"),
                quantity = optionalJsonInt(json["quantity"], "quantity"),
                sku = requireJsonString(json["sku"], "sku"),
                withOffer = optionalJsonObject(json["withOffer"], "withOffer")?.let { DiscountOfferInputIOS.fromJson(it) },
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("andDangerouslyFinishTransactionAutomatically", andDangerouslyFinishTransactionAutomatically?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("appAccountToken", appAccountToken?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("quantity", quantity?.let { value -> JsonPrimitive(value) } ?: JsonNull)
        put("sku", JsonPrimitive(sku))
        put("withOffer", withOffer?.let { value -> value.toJson() } ?: JsonNull)
    }
}

public data class RequestSubscriptionPropsByPlatforms(
    /**
     * Android-specific subscription parameters
     */
    val android: RequestSubscriptionAndroidProps? = null,
    /**
     * iOS-specific subscription parameters
     */
    val ios: RequestSubscriptionIosProps? = null
) {
    companion object {
        fun fromJson(json: JsonObject): RequestSubscriptionPropsByPlatforms {
            return RequestSubscriptionPropsByPlatforms(
                android = optionalJsonObject(json["android"], "android")?.let { RequestSubscriptionAndroidProps.fromJson(it) },
                ios = optionalJsonObject(json["ios"], "ios")?.let { RequestSubscriptionIosProps.fromJson(it) },
            )
        }
    }

    fun toJson(): JsonObject = buildJsonObject {
        put("android", android?.let { value -> value.toJson() } ?: JsonNull)
        put("ios", ios?.let { value -> value.toJson() } ?: JsonNull)
    }
}

// MARK: - Unions

public sealed interface Product : ProductCommon {
    fun toJson(): JsonObject

    companion object {
        fun fromJson(json: JsonObject): Product {
            val typeName = requireJsonString(json["__typename"], "__typename")
            return when (typeName) {
                "ProductAndroid" -> ProductAndroid.fromJson(json)
                "ProductIOS" -> ProductIOS.fromJson(json)
                else -> throw IllegalArgumentException("Unknown __typename for Product: " + typeName)
            }
        }
    }
}

public sealed interface ProductSubscription : ProductCommon {
    fun toJson(): JsonObject

    companion object {
        fun fromJson(json: JsonObject): ProductSubscription {
            val typeName = requireJsonString(json["__typename"], "__typename")
            return when (typeName) {
                "ProductSubscriptionAndroid" -> ProductSubscriptionAndroid.fromJson(json)
                "ProductSubscriptionIOS" -> ProductSubscriptionIOS.fromJson(json)
                else -> throw IllegalArgumentException("Unknown __typename for ProductSubscription: " + typeName)
            }
        }
    }
}

public sealed interface Purchase : PurchaseCommon {
    fun toJson(): JsonObject

    companion object {
        fun fromJson(json: JsonObject): Purchase {
            val typeName = requireJsonString(json["__typename"], "__typename")
            return when (typeName) {
                "PurchaseAndroid" -> PurchaseAndroid.fromJson(json)
                "PurchaseIOS" -> PurchaseIOS.fromJson(json)
                else -> throw IllegalArgumentException("Unknown __typename for Purchase: " + typeName)
            }
        }
    }
}

public sealed interface ReceiptValidationResult {
    fun toJson(): JsonObject

    companion object {
        fun fromJson(json: JsonObject): ReceiptValidationResult {
            val typeName = requireJsonString(json["__typename"], "__typename")
            return when (typeName) {
                "ReceiptValidationResultAndroid" -> ReceiptValidationResultAndroid.fromJson(json)
                "ReceiptValidationResultIOS" -> ReceiptValidationResultIOS.fromJson(json)
                else -> throw IllegalArgumentException("Unknown __typename for ReceiptValidationResult: " + typeName)
            }
        }
    }
}

// MARK: - Root Operations

/**
 * GraphQL root mutation operations.
 */
public interface MutationResolver {
    /**
     * Acknowledge a non-consumable purchase or subscription
     */
    suspend fun acknowledgePurchaseAndroid(purchaseToken: String): Boolean
    /**
     * Initiate a refund request for a product (iOS 15+)
     */
    suspend fun beginRefundRequestIOS(sku: String): String?
    /**
     * Clear pending transactions from the StoreKit payment queue
     */
    suspend fun clearTransactionIOS(): Boolean
    /**
     * Consume a purchase token so it can be repurchased
     */
    suspend fun consumePurchaseAndroid(purchaseToken: String): Boolean
    /**
     * Open the native subscription management surface
     */
    suspend fun deepLinkToSubscriptions(options: DeepLinkOptions? = null): Unit
    /**
     * Close the platform billing connection
     */
    suspend fun endConnection(): Boolean
    /**
     * Finish a transaction after validating receipts
     */
    suspend fun finishTransaction(purchase: PurchaseInput, isConsumable: Boolean? = null): Unit
    /**
     * Establish the platform billing connection
     */
    suspend fun initConnection(): Boolean
    /**
     * Present the App Store code redemption sheet
     */
    suspend fun presentCodeRedemptionSheetIOS(): Boolean
    /**
     * Initiate a purchase flow; rely on events for final state
     */
    suspend fun requestPurchase(params: RequestPurchaseProps): RequestPurchaseResult?
    /**
     * Purchase the promoted product surfaced by the App Store
     */
    suspend fun requestPurchaseOnPromotedProductIOS(): Boolean
    /**
     * Restore completed purchases across platforms
     */
    suspend fun restorePurchases(): Unit
    /**
     * Open subscription management UI and return changed purchases (iOS 15+)
     */
    suspend fun showManageSubscriptionsIOS(): List<PurchaseIOS>
    /**
     * Force a StoreKit sync for transactions (iOS 15+)
     */
    suspend fun syncIOS(): Boolean
    /**
     * Validate purchase receipts with the configured providers
     */
    suspend fun validateReceipt(options: ReceiptValidationProps): ReceiptValidationResult
}

/**
 * GraphQL root query operations.
 */
public interface QueryResolver {
    /**
     * Get current StoreKit 2 entitlements (iOS 15+)
     */
    suspend fun currentEntitlementIOS(sku: String): PurchaseIOS?
    /**
     * Retrieve products or subscriptions from the store
     */
    suspend fun fetchProducts(params: ProductRequest): FetchProductsResult
    /**
     * Get active subscriptions (filters by subscriptionIds when provided)
     */
    suspend fun getActiveSubscriptions(subscriptionIds: List<String>? = null): List<ActiveSubscription>
    /**
     * Fetch the current app transaction (iOS 16+)
     */
    suspend fun getAppTransactionIOS(): AppTransaction?
    /**
     * Get all available purchases for the current user
     */
    suspend fun getAvailablePurchases(options: PurchaseOptions? = null): List<Purchase>
    /**
     * Retrieve all pending transactions in the StoreKit queue
     */
    suspend fun getPendingTransactionsIOS(): List<PurchaseIOS>
    /**
     * Get the currently promoted product (iOS 11+)
     */
    suspend fun getPromotedProductIOS(): ProductIOS?
    /**
     * Get base64-encoded receipt data for validation
     */
    suspend fun getReceiptDataIOS(): String?
    /**
     * Get the current App Store storefront country code
     */
    suspend fun getStorefrontIOS(): String
    /**
     * Get the transaction JWS (StoreKit 2)
     */
    suspend fun getTransactionJwsIOS(sku: String): String?
    /**
     * Check whether the user has active subscriptions
     */
    suspend fun hasActiveSubscriptions(subscriptionIds: List<String>? = null): Boolean
    /**
     * Check introductory offer eligibility for a subscription group
     */
    suspend fun isEligibleForIntroOfferIOS(groupID: String): Boolean
    /**
     * Verify a StoreKit 2 transaction signature
     */
    suspend fun isTransactionVerifiedIOS(sku: String): Boolean
    /**
     * Get the latest transaction for a product using StoreKit 2
     */
    suspend fun latestTransactionIOS(sku: String): PurchaseIOS?
    /**
     * Get StoreKit 2 subscription status details (iOS 15+)
     */
    suspend fun subscriptionStatusIOS(sku: String): List<SubscriptionStatusIOS>
    /**
     * Validate a receipt for a specific product
     */
    suspend fun validateReceiptIOS(options: ReceiptValidationProps): ReceiptValidationResultIOS
}

/**
 * GraphQL root subscription operations.
 */
public interface SubscriptionResolver {
    /**
     * Fires when the App Store surfaces a promoted product (iOS only)
     */
    suspend fun promotedProductIOS(): String
    /**
     * Fires when a purchase fails or is cancelled
     */
    suspend fun purchaseError(): PurchaseError
    /**
     * Fires when a purchase completes successfully or a pending purchase resolves
     */
    suspend fun purchaseUpdated(): Purchase
}

// MARK: - Root Operation Helpers

// MARK: - Mutation Helpers

public typealias MutationAcknowledgePurchaseAndroidHandler = suspend (purchaseToken: String) -> Boolean
public typealias MutationBeginRefundRequestIOSHandler = suspend (sku: String) -> String?
public typealias MutationClearTransactionIOSHandler = suspend () -> Boolean
public typealias MutationConsumePurchaseAndroidHandler = suspend (purchaseToken: String) -> Boolean
public typealias MutationDeepLinkToSubscriptionsHandler = suspend (options: DeepLinkOptions?) -> Unit
public typealias MutationEndConnectionHandler = suspend () -> Boolean
public typealias MutationFinishTransactionHandler = suspend (purchase: PurchaseInput, isConsumable: Boolean?) -> Unit
public typealias MutationInitConnectionHandler = suspend () -> Boolean
public typealias MutationPresentCodeRedemptionSheetIOSHandler = suspend () -> Boolean
public typealias MutationRequestPurchaseHandler = suspend (params: RequestPurchaseProps) -> RequestPurchaseResult?
public typealias MutationRequestPurchaseOnPromotedProductIOSHandler = suspend () -> Boolean
public typealias MutationRestorePurchasesHandler = suspend () -> Unit
public typealias MutationShowManageSubscriptionsIOSHandler = suspend () -> List<PurchaseIOS>
public typealias MutationSyncIOSHandler = suspend () -> Boolean
public typealias MutationValidateReceiptHandler = suspend (options: ReceiptValidationProps) -> ReceiptValidationResult

public data class MutationHandlers(
    val acknowledgePurchaseAndroid: MutationAcknowledgePurchaseAndroidHandler? = null,
    val beginRefundRequestIOS: MutationBeginRefundRequestIOSHandler? = null,
    val clearTransactionIOS: MutationClearTransactionIOSHandler? = null,
    val consumePurchaseAndroid: MutationConsumePurchaseAndroidHandler? = null,
    val deepLinkToSubscriptions: MutationDeepLinkToSubscriptionsHandler? = null,
    val endConnection: MutationEndConnectionHandler? = null,
    val finishTransaction: MutationFinishTransactionHandler? = null,
    val initConnection: MutationInitConnectionHandler? = null,
    val presentCodeRedemptionSheetIOS: MutationPresentCodeRedemptionSheetIOSHandler? = null,
    val requestPurchase: MutationRequestPurchaseHandler? = null,
    val requestPurchaseOnPromotedProductIOS: MutationRequestPurchaseOnPromotedProductIOSHandler? = null,
    val restorePurchases: MutationRestorePurchasesHandler? = null,
    val showManageSubscriptionsIOS: MutationShowManageSubscriptionsIOSHandler? = null,
    val syncIOS: MutationSyncIOSHandler? = null,
    val validateReceipt: MutationValidateReceiptHandler? = null
)

// MARK: - Query Helpers

public typealias QueryCurrentEntitlementIOSHandler = suspend (sku: String) -> PurchaseIOS?
public typealias QueryFetchProductsHandler = suspend (params: ProductRequest) -> FetchProductsResult
public typealias QueryGetActiveSubscriptionsHandler = suspend (subscriptionIds: List<String>?) -> List<ActiveSubscription>
public typealias QueryGetAppTransactionIOSHandler = suspend () -> AppTransaction?
public typealias QueryGetAvailablePurchasesHandler = suspend (options: PurchaseOptions?) -> List<Purchase>
public typealias QueryGetPendingTransactionsIOSHandler = suspend () -> List<PurchaseIOS>
public typealias QueryGetPromotedProductIOSHandler = suspend () -> ProductIOS?
public typealias QueryGetReceiptDataIOSHandler = suspend () -> String?
public typealias QueryGetStorefrontIOSHandler = suspend () -> String
public typealias QueryGetTransactionJwsIOSHandler = suspend (sku: String) -> String?
public typealias QueryHasActiveSubscriptionsHandler = suspend (subscriptionIds: List<String>?) -> Boolean
public typealias QueryIsEligibleForIntroOfferIOSHandler = suspend (groupID: String) -> Boolean
public typealias QueryIsTransactionVerifiedIOSHandler = suspend (sku: String) -> Boolean
public typealias QueryLatestTransactionIOSHandler = suspend (sku: String) -> PurchaseIOS?
public typealias QuerySubscriptionStatusIOSHandler = suspend (sku: String) -> List<SubscriptionStatusIOS>
public typealias QueryValidateReceiptIOSHandler = suspend (options: ReceiptValidationProps) -> ReceiptValidationResultIOS

public data class QueryHandlers(
    val currentEntitlementIOS: QueryCurrentEntitlementIOSHandler? = null,
    val fetchProducts: QueryFetchProductsHandler? = null,
    val getActiveSubscriptions: QueryGetActiveSubscriptionsHandler? = null,
    val getAppTransactionIOS: QueryGetAppTransactionIOSHandler? = null,
    val getAvailablePurchases: QueryGetAvailablePurchasesHandler? = null,
    val getPendingTransactionsIOS: QueryGetPendingTransactionsIOSHandler? = null,
    val getPromotedProductIOS: QueryGetPromotedProductIOSHandler? = null,
    val getReceiptDataIOS: QueryGetReceiptDataIOSHandler? = null,
    val getStorefrontIOS: QueryGetStorefrontIOSHandler? = null,
    val getTransactionJwsIOS: QueryGetTransactionJwsIOSHandler? = null,
    val hasActiveSubscriptions: QueryHasActiveSubscriptionsHandler? = null,
    val isEligibleForIntroOfferIOS: QueryIsEligibleForIntroOfferIOSHandler? = null,
    val isTransactionVerifiedIOS: QueryIsTransactionVerifiedIOSHandler? = null,
    val latestTransactionIOS: QueryLatestTransactionIOSHandler? = null,
    val subscriptionStatusIOS: QuerySubscriptionStatusIOSHandler? = null,
    val validateReceiptIOS: QueryValidateReceiptIOSHandler? = null
)

// MARK: - Subscription Helpers

public typealias SubscriptionPromotedProductIOSHandler = suspend () -> String
public typealias SubscriptionPurchaseErrorHandler = suspend () -> PurchaseError
public typealias SubscriptionPurchaseUpdatedHandler = suspend () -> Purchase

public data class SubscriptionHandlers(
    val promotedProductIOS: SubscriptionPromotedProductIOSHandler? = null,
    val purchaseError: SubscriptionPurchaseErrorHandler? = null,
    val purchaseUpdated: SubscriptionPurchaseUpdatedHandler? = null
)
