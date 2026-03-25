package com.dawn.fade.data.model.base

/**
 * 通用数据层结果封装，统一向上层暴露成功与失败两种状态。
 */
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>

    data class Error(
        val message: String,
        val errorCode: Int? = null,
        val throwable: Throwable? = null
    ) : ApiResult<Nothing>
}
