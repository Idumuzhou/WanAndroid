package com.dawn.fade.data.model.base

/**
 * 通用接口响应体，统一承载业务数据和服务端错误信息。
 */
data class BaseResponse<T>(
    val data: T,
    val errorCode: Int,
    val errorMsg: String
)
