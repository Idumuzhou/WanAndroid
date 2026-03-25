package com.dawn.fade.theme.settings

import androidx.appcompat.app.AppCompatDelegate

enum class AppThemeMode(
    val storageValue: String,
    val nightMode: Int
) {
    FollowSystem("follow_system", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    Light("light", AppCompatDelegate.MODE_NIGHT_NO),
    Dark("dark", AppCompatDelegate.MODE_NIGHT_YES);

    companion object {
        fun fromStorageValue(value: String?): AppThemeMode {
            return entries.firstOrNull { it.storageValue == value } ?: FollowSystem
        }
    }
}
