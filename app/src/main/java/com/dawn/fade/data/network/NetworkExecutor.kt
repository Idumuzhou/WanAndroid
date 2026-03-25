package com.dawn.fade.data.network

import com.dawn.fade.data.model.base.ApiResult
import com.dawn.fade.data.model.base.BaseResponse

/**
 * 通用网络执行器，统一处理接口业务码和异常兜底。
 */
suspend fun <T> executeApiCall(
    request: suspend () -> BaseResponse<T>
): ApiResult<T> {
    return runCatching {
        request()
    }.fold(
        onSuccess = { response ->
            if (response.errorCode == 0) {
                ApiResult.Success(response.data)
            } else {
                ApiResult.Error(
                    message = response.errorMsg.ifBlank { "接口请求失败" },
                    errorCode = response.errorCode
                )
            }
        },
        onFailure = { throwable ->
            ApiResult.Error(
                message = throwable.message ?: "网络请求失败，请稍后重试",
                errorCode = null,
                throwable = throwable
            )
        }
    )
}
