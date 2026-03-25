package com.dawn.fade.log

import timber.log.Timber

/**
 * 应用日志工具，统一封装业务日志输出格式，避免散落使用原生日志接口。
 */
object AppLogger {
    var isDebugLoggingEnabled: Boolean = false
        private set

    fun initialize(isDebuggable: Boolean) {
        isDebugLoggingEnabled = isDebuggable
    }

    fun d(tag: String, message: String) {
        Timber.tag(tag).d(message)
    }

    fun i(tag: String, message: String) {
        Timber.tag(tag).i(message)
    }

    fun e(tag: String, throwable: Throwable? = null, message: String) {
        if (throwable == null) {
            Timber.tag(tag).e(message)
        } else {
            Timber.tag(tag).e(throwable, message)
        }
    }
}
