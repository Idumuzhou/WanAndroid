package com.dawn.fade

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dawn.fade.R
import com.dawn.fade.theme.settings.ThemePreferenceStore

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePreferenceStore.applyThemeMode(this)
        applyOrientationPolicy()
        super.onCreate(savedInstanceState)
        applySystemBarStyle()
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        super.onMultiWindowModeChanged(isInMultiWindowMode)
        applyOrientationPolicy()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    protected fun applyOrientationPolicy() {
        requestedOrientation = if (shouldLockPortrait()) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    protected open fun shouldLockPortrait(): Boolean {
        val isCompactPhone = resources.configuration.smallestScreenWidthDp < 600
        val isFoldable = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_HINGE_ANGLE)
        return isCompactPhone && !isFoldable && !isInMultiWindowMode
    }

    protected open fun applySystemBarStyle() {
        updateSystemBarColor(R.color.screen_background)
    }

    protected fun updateSystemBarColor(@ColorRes colorRes: Int) {
        val color = ContextCompat.getColor(this, colorRes)
        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

        window.statusBarColor = color
        window.navigationBarColor = color

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isNightMode
            isAppearanceLightNavigationBars = !isNightMode
        }
    }
}
