package com.dawn.fade.theme.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemePreferenceStore {
    private const val PreferenceName = "theme_preferences"
    private const val KeyThemeMode = "theme_mode"

    fun getThemeMode(context: Context): AppThemeMode {
        val preferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        return AppThemeMode.fromStorageValue(preferences.getString(KeyThemeMode, null))
    }

    fun setThemeMode(context: Context, mode: AppThemeMode) {
        val preferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        preferences.edit().putString(KeyThemeMode, mode.storageValue).apply()
        AppCompatDelegate.setDefaultNightMode(mode.nightMode)
    }

    fun applyThemeMode(context: Context) {
        AppCompatDelegate.setDefaultNightMode(getThemeMode(context).nightMode)
    }
}
