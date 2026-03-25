package com.dawn.fade.data.network

import com.dawn.fade.data.network.service.ApiService

/**
 * WanAndroid 接口客户端，集中维护 Retrofit 初始化和服务实例。
 */
object WanAndroidApiClient {
    private const val BaseUrl = "https://www.wanandroid.com/"

    val service: ApiService by lazy {
        RetrofitFactory.createService(BaseUrl)
    }
}
