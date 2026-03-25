package com.dawn.fade.data.network.cookie

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Cookie
import okhttp3.HttpUrl

/**
 * Cookie 存储器，负责持久化 WanAndroid 登录 Cookie 以支持自动登录。
 */
object CookieStore {
    private const val PreferenceName = "cookie_preferences"
    private const val KeyCookies = "cookies"

    private lateinit var appContext: Context
    private val gson = Gson()
    private val typeToken = object : TypeToken<MutableMap<String, MutableList<String>>>() {}
    private val memoryCookies = mutableMapOf<String, MutableList<String>>()

    fun initialize(context: Context) {
        appContext = context.applicationContext
        memoryCookies.clear()
        memoryCookies.putAll(readPersistedCookies())
    }

    fun saveCookies(url: HttpUrl, cookies: List<Cookie>) {
        val key = url.host()
        memoryCookies[key] = cookies.map { it.toString() }.toMutableList()
        persistCookies()
    }

    fun loadCookies(url: HttpUrl): List<Cookie> {
        return memoryCookies[url.host()]
            .orEmpty()
            .mapNotNull { Cookie.parse(url, it) }
    }

    fun clear() {
        memoryCookies.clear()
        if (::appContext.isInitialized) {
            appContext.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
                .edit()
                .remove(KeyCookies)
                .apply()
        }
    }

    private fun persistCookies() {
        if (!::appContext.isInitialized) {
            return
        }

        appContext.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
            .edit()
            .putString(KeyCookies, gson.toJson(memoryCookies))
            .apply()
    }

    private fun readPersistedCookies(): MutableMap<String, MutableList<String>> {
        if (!::appContext.isInitialized) {
            return mutableMapOf()
        }

        val cookieJson = appContext.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
            .getString(KeyCookies, null)
            ?: return mutableMapOf()

        return runCatching {
            gson.fromJson<MutableMap<String, MutableList<String>>>(cookieJson, typeToken.type)
        }.getOrDefault(mutableMapOf())
    }
}
