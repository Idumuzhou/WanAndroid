package com.dawn.fade.log

import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

/**
 * 网络日志拦截器，统一打印请求方法、地址、请求体和响应结果。
 */
class ApiLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBody = request.body()
        val requestBuffer = Buffer()
        requestBody?.writeTo(requestBuffer)

        val requestLog = buildString {
            appendLine("${request.method()} ${request.url()}")
            appendLine("Headers:")
            request.headers().names().forEach { name ->
                val safeValue = if (name.equals("Cookie", ignoreCase = true)) {
                    "***"
                } else {
                    request.header(name).orEmpty()
                }
                appendLine("$name: $safeValue")
            }
            if (requestBody != null) {
                appendLine("Body:")
                append(requestBuffer.readUtf8())
            }
        }.trim()

        val requestStartAtMillis = System.currentTimeMillis()
        val startNs = System.nanoTime()
        return try {
            val response = chain.proceed(request)
            val tookMs = (System.nanoTime() - startNs) / 1_000_000
            val responseBody = response.peekBody(1024 * 1024)

            val responseLog = buildString {
                appendLine("${response.code()} ${response.message()} (${tookMs}ms)")
                appendLine("URL: ${response.request().url()}")
                appendLine("Headers:")
                response.headers().names().forEach { name ->
                    val safeValue = if (name.equals("Set-Cookie", ignoreCase = true)) {
                        "***"
                    } else {
                        response.header(name).orEmpty()
                    }
                    appendLine("$name: $safeValue")
                }
                appendLine("Body:")
                append(responseBody.string())
            }.trim()

            HttpLogFormatter.logTransaction(
                request = requestLog,
                result = responseLog,
                isSuccessful = response.isSuccessful(),
                requestStartAtMillis = requestStartAtMillis
            )

            response
        } catch (throwable: Throwable) {
            HttpLogFormatter.logTransaction(
                request = requestLog,
                result = "Request failed: ${request.method()} ${request.url()}\n${throwable.message.orEmpty()}",
                isSuccessful = false,
                requestStartAtMillis = requestStartAtMillis,
                throwable = throwable
            )
            throw throwable
        }
    }
}
