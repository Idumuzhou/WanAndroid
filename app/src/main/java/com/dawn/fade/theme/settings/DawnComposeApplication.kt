package com.dawn.fade.theme.settings

import android.app.Application
import android.content.pm.ApplicationInfo
import com.dawn.fade.data.model.base.ApiResult
import com.dawn.fade.data.network.cookie.CookieStore
import com.dawn.fade.data.repository.Repository
import com.dawn.fade.data.session.UserSessionStore
import com.dawn.fade.log.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class DawnComposeApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repository = Repository()

    override fun onCreate() {
        super.onCreate()
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        AppLogger.initialize(isDebuggable)
        if (isDebuggable) {
            Timber.plant(Timber.DebugTree())
        }
        CookieStore.initialize(this)
        ThemePreferenceStore.applyThemeMode(this)
        syncUserInfoIfNeeded()
    }

    private fun syncUserInfoIfNeeded() {
        if (!UserSessionStore.isLoggedIn(this)) {
            return
        }

        applicationScope.launch {
            when (val result = repository.fetchUserInfo()) {
                is ApiResult.Success -> {
                    val previousUser = UserSessionStore.getUser(this@DawnComposeApplication)
                    UserSessionStore.saveUser(
                        this@DawnComposeApplication,
                        result.data.toUserData(previousUser)
                    )
                }
                is ApiResult.Error -> {
                    if (result.errorCode == -1001) {
                        UserSessionStore.clear(this@DawnComposeApplication)
                        CookieStore.clear()
                    }
                }
            }
        }
    }
}
