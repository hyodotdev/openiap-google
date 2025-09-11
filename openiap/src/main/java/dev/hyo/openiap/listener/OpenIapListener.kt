package dev.hyo.openiap.listener

import dev.hyo.openiap.models.OpenIapPurchase
import dev.hyo.openiap.OpenIapError

/**
 * Listener for purchase updates
 */
fun interface OpenIapPurchaseUpdateListener {
    /**
     * Called when a purchase is updated
     * @param purchase The updated purchase
     */
    fun onPurchaseUpdated(purchase: OpenIapPurchase)
}

/**
 * Listener for purchase errors
 */
fun interface OpenIapPurchaseErrorListener {
    /**
     * Called when a purchase error occurs
     * @param error The error that occurred
     */
    fun onPurchaseError(error: OpenIapError)
}

/**
 * Combined listener interface for convenience
 */
interface OpenIapListener : OpenIapPurchaseUpdateListener, OpenIapPurchaseErrorListener
