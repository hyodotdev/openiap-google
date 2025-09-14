package dev.hyo.martie

object IapConstants {
    private fun isHorizon(): Boolean =
        dev.hyo.martie.BuildConfig.OPENIAP_STORE.equals("horizon", ignoreCase = true)

    // Define your Horizon product IDs here (placeholders)
    private val HORIZON_INAPP = listOf(
        "dev.hyo.martie.10bulbs",
        "dev.hyo.martie.30bulbs",
    )
    private val HORIZON_SUBS = listOf(
        "dev.hyo.martie.premium",
    )

    // Google Play product IDs (existing)
    private val PLAY_INAPP = listOf(
        "dev.hyo.martie.10bulbs",
        "dev.hyo.martie.30bulbs",
    )
    private val PLAY_SUBS = listOf(
        "dev.hyo.martie.premium",
    )

    fun inappSkus(): List<String> = if (isHorizon()) HORIZON_INAPP else PLAY_INAPP
    fun subsSkus(): List<String> = if (isHorizon()) HORIZON_SUBS else PLAY_SUBS
}
