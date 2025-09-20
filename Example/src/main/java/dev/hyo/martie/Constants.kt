package dev.hyo.martie

object IapConstants {
    private fun isHorizon(): Boolean =
        dev.hyo.martie.BuildConfig.OPENIAP_STORE.equals("horizon", ignoreCase = true)

    private val HORIZON_INAPP = listOf(
        "dev.hyo.martie.10bulbs",
        "dev.hyo.martie.30bulbs",
    )
    private val HORIZON_SUBS = listOf(
        "dev.hyo.martie.premium",
    )

    private val PLAY_INAPP = listOf(
        "dev.hyo.martie.10bulbs",
        "dev.hyo.martie.30bulbs",
    )
    private val PLAY_SUBS = listOf(
        "dev.hyo.martie.premium",
    )

    val INAPP_SKUS: List<String>
        get() = if (isHorizon()) HORIZON_INAPP else PLAY_INAPP

    val SUBS_SKUS: List<String>
        get() = if (isHorizon()) HORIZON_SUBS else PLAY_SUBS
}
