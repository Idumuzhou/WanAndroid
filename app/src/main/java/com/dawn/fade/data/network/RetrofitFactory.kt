package com.dawn.fade.data.network

import com.dawn.fade.data.network.cookie.PersistentCookieJar
import com.dawn.fade.log.AppLogger
import com.dawn.fade.log.ApiLoggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 通用 Retrofit 工厂，统一管理网络服务实例的创建方式。
 */
object RetrofitFactory {
    private val retrofitCache = mutableMapOf<String, Retrofit>()
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            cookieJar(PersistentCookieJar)
            if (AppLogger.isDebugLoggingEnabled) {
                addInterceptor(ApiLoggingInterceptor())
            }
            connectTimeout(15, TimeUnit.SECONDS)
            readTimeout(15, TimeUnit.SECONDS)
            writeTimeout(15, TimeUnit.SECONDS)
        }.build()
    }

    @PublishedApi
    internal fun getRetrofit(baseUrl: String): Retrofit {
        return retrofitCache.getOrPut(baseUrl) {
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }

    inline fun <reified T> createService(baseUrl: String): T {
        return getRetrofit(baseUrl).create(T::class.java)
    }
}
