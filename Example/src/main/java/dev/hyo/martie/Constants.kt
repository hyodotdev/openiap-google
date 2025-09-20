package dev.hyo.martie

object IapConstants {
    private fun isHorizon(): Boolean =
        dev.hyo.martie.BuildConfig.OPENIAP_STORE.equals("horizon", ignoreCase = true)

    private val HORIZON_INAPP = listOf(
        "dev.hyo.martie.10bulbs",
        "dev.hyo.martie.30bulbs",
        "dev.hyo.martie.certified"  // Non-consumable
    )

    val SUBS_SKUS = listOf(
        "dev.hyo.martie.premium",      // Main subscription with multiple offers
        "dev.hyo.martie.premium_year"  // Separate yearly subscription product
    )

    // Base plan IDs for dev.hyo.martie.premium subscription
    const val PREMIUM_MONTHLY_BASE_PLAN = "premium"       // Monthly base plan
    const val PREMIUM_YEARLY_BASE_PLAN = "premium-year"   // Yearly base plan
}

    val INAPP_SKUS: List<String>
        get() = if (isHorizon()) HORIZON_INAPP else PLAY_INAPP

    val SUBS_SKUS: List<String>
        get() = if (isHorizon()) HORIZON_SUBS else PLAY_SUBS
}
