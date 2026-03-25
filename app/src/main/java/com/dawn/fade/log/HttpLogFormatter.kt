package com.dawn.fade.log

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * HTTP 日志格式化工具，统一整理网络请求与响应打印的显示格式。
 */
object HttpLogFormatter {
    private const val HttpTag = "HTTP"
    private const val TransactionDivider = "############################################################"
    private const val RequestDivider = "================ REQUEST ================"
    private const val ResponseDivider = "================ RESPONSE ================"
    private const val ErrorDivider = "================ ERROR ================="
    private val timeFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    fun logTransaction(
        request: String,
        result: String,
        isSuccessful: Boolean,
        requestStartAtMillis: Long,
        throwable: Throwable? = null
    ) {
        val resultDivider = if (throwable == null) ResponseDivider else ErrorDivider
        val formattedMessage = buildString {
            appendLine(buildInterfaceDivider(request, requestStartAtMillis))
            appendLine(RequestDivider)
            appendLine(prettify(request))
            appendLine(resultDivider)
            append(prettify(result))
        }.trim()

        logRedDivider()

        if (isSuccessful) {
            AppLogger.d(HttpTag, formattedMessage)
        } else {
            AppLogger.e(tag = HttpTag, throwable = throwable, message = formattedMessage)
        }

        logRedDivider()
    }

    private fun logRedDivider() {
        AppLogger.e(tag = HttpTag, message = TransactionDivider)
    }

    private fun prettify(message: String): String {
        return message.lineSequence()
            .joinToString("\n") { line ->
                val normalized = line.trim()
                when {
                    normalized.startsWith("{") && normalized.endsWith("}") -> formatJsonObject(normalized)
                    normalized.startsWith("[") && normalized.endsWith("]") -> formatJsonArray(normalized)
                    else -> line
                }
            }
    }

    private fun formatJsonObject(content: String): String {
        return runCatching {
            JSONObject(content).toString(2)
        }.getOrDefault(content)
    }

    private fun formatJsonArray(content: String): String {
        return runCatching {
            JSONArray(content).toString(2)
        }.getOrDefault(content)
    }

    private fun buildInterfaceDivider(message: String, requestStartAtMillis: Long): String {
        val firstLine = message.lineSequence().firstOrNull().orEmpty()
        val parts = firstLine.split(" ")
        val method = parts.getOrNull(0).orEmpty()
        val url = parts.getOrNull(1).orEmpty()
        val apiName = url.substringAfter("://", url)
            .substringAfter("/", url)
            .substringBefore("?")
            .ifBlank { "unknown" }

        return "■■ $method $apiName ${timeFormatter.format(Date(requestStartAtMillis))} ■■"
    }
}
