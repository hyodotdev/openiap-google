package dev.hyo.openiap.models

import com.google.gson.Gson

/** Simple Gson-based serialization helpers (parity with Apple file naming) */
object OpenIapSerialization {
    private val gson = Gson()

    fun toJson(obj: Any?): String = gson.toJson(obj)

    fun <T> fromJson(json: String?, clazz: Class<T>): T? =
        runCatching { gson.fromJson(json, clazz) }.getOrNull()
}
