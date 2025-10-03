package dev.hyo.openiap.utils

import com.meta.horizon.billingclient.api.ProductDetails as HorizonProductDetails
import com.meta.horizon.billingclient.api.Purchase as HorizonPurchase
import dev.hyo.openiap.ActiveSubscription
import dev.hyo.openiap.IapPlatform
import dev.hyo.openiap.PricingPhaseAndroid
import dev.hyo.openiap.PricingPhasesAndroid
import dev.hyo.openiap.ProductAndroid
import dev.hyo.openiap.ProductAndroidOneTimePurchaseOfferDetail
import dev.hyo.openiap.ProductSubscriptionAndroid
import dev.hyo.openiap.ProductSubscriptionAndroidOfferDetails
import dev.hyo.openiap.ProductType
import dev.hyo.openiap.PurchaseAndroid
import dev.hyo.openiap.PurchaseState

internal object HorizonBillingConverters {

    fun HorizonProductDetails.toInAppProduct(): ProductAndroid {
        val offer = oneTimePurchaseOfferDetails
        val displayPrice = offer?.formattedPrice.orEmpty()
        val currency = offer?.priceCurrencyCode.orEmpty()
        val priceAmountMicros = offer?.priceAmountMicros ?: 0L

        return ProductAndroid(
            currency = currency,
            debugDescription = description,
            description = description,
            displayName = name,
            displayPrice = displayPrice,
            id = productId,
            nameAndroid = name,
            oneTimePurchaseOfferDetailsAndroid = offer?.let {
                ProductAndroidOneTimePurchaseOfferDetail(
                    formattedPrice = it.formattedPrice,
                    priceAmountMicros = it.priceAmountMicros.toString(),
                    priceCurrencyCode = it.priceCurrencyCode
                )
            },
            platform = IapPlatform.Android,
            price = priceAmountMicros.toDouble() / 1_000_000.0,
            subscriptionOfferDetailsAndroid = null,
            title = title,
            type = ProductType.InApp
        )
    }

    fun HorizonProductDetails.toSubscriptionProduct(): ProductSubscriptionAndroid {
        val offers = subscriptionOfferDetails.orEmpty()
        val firstPhase = offers.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()
        val displayPrice = firstPhase?.formattedPrice.orEmpty()
        val currency = firstPhase?.priceCurrencyCode.orEmpty()

        val pricingDetails = offers.map { offer ->
            ProductSubscriptionAndroidOfferDetails(
                basePlanId = offer.basePlanId,
                offerId = offer.offerId,
                offerTags = offer.offerTags,
                offerToken = offer.offerToken,
                pricingPhases = PricingPhasesAndroid(
                    pricingPhaseList = offer.pricingPhases.pricingPhaseList.map { phase ->
                        PricingPhaseAndroid(
                            billingCycleCount = phase.billingCycleCount,
                            billingPeriod = phase.billingPeriod,
                            formattedPrice = phase.formattedPrice,
                            priceAmountMicros = phase.priceAmountMicros.toString(),
                            priceCurrencyCode = phase.priceCurrencyCode,
                            recurrenceMode = phase.recurrenceMode,
                        )
                    }
                )
            )
        }

        return ProductSubscriptionAndroid(
            currency = currency,
            debugDescription = description,
            description = description,
            displayName = name,
            displayPrice = displayPrice,
            id = productId,
            nameAndroid = name,
            oneTimePurchaseOfferDetailsAndroid = oneTimePurchaseOfferDetails?.let {
                ProductAndroidOneTimePurchaseOfferDetail(
                    formattedPrice = it.formattedPrice,
                    priceAmountMicros = it.priceAmountMicros.toString(),
                    priceCurrencyCode = it.priceCurrencyCode
                )
            },
            platform = IapPlatform.Android,
            price = firstPhase?.priceAmountMicros?.toDouble()?.div(1_000_000.0),
            subscriptionOfferDetailsAndroid = pricingDetails,
            title = title,
            type = ProductType.Subs
        )
    }

    fun HorizonPurchase.toPurchase(productType: String): PurchaseAndroid {
        val token = purchaseToken
        val productsList = products ?: emptyList()
        val purchaseState = PurchaseState.Purchased

        return PurchaseAndroid(
            autoRenewingAndroid = isAutoRenewing(),
            dataAndroid = originalJson,
            developerPayloadAndroid = developerPayload,
            id = orderId ?: token,
            ids = productsList,
            isAcknowledgedAndroid = isAcknowledged(),
            isAutoRenewing = isAutoRenewing(),
            obfuscatedAccountIdAndroid = null,
            obfuscatedProfileIdAndroid = null,
            packageNameAndroid = packageName,
            platform = IapPlatform.Android,
            productId = productsList.firstOrNull().orEmpty(),
            purchaseState = purchaseState,
            purchaseToken = token,
            quantity = quantity ?: 1,
            signatureAndroid = signature,
            transactionDate = (purchaseTime ?: 0L).toDouble(),
            transactionId = orderId ?: token
        )
    }

    fun HorizonPurchase.toActiveSubscription(): ActiveSubscription = ActiveSubscription(
        autoRenewingAndroid = isAutoRenewing(),
        isActive = true,
        productId = products?.firstOrNull().orEmpty(),
        purchaseToken = purchaseToken,
        transactionDate = (purchaseTime ?: 0L).toDouble(),
        transactionId = orderId ?: purchaseToken
    )
}
