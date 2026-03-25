package com.dawn.fade.data.network.cookie

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * 持久化 CookieJar，统一接管网络层 Cookie 的保存和读取。
 */
object PersistentCookieJar : CookieJar {
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        CookieStore.saveCookies(url, cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return CookieStore.loadCookies(url)
    }
}
