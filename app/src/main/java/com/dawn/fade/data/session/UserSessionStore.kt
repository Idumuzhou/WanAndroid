package com.dawn.fade.data.session

import android.content.Context
import com.dawn.fade.data.model.user.UserData
import com.google.gson.Gson

/**
 * 用户会话存储，负责持久化登录用户信息并提供登录态读取能力。
 */
object UserSessionStore {
    private const val PreferenceName = "user_session_preferences"
    private const val KeyUserJson = "user_json"

    private val gson = Gson()

    fun saveUser(context: Context, user: UserData) {
        val preferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        preferences.edit().putString(KeyUserJson, gson.toJson(user)).apply()
    }

    fun getUser(context: Context): UserData? {
        val preferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        val userJson = preferences.getString(KeyUserJson, null) ?: return null
        return runCatching { gson.fromJson(userJson, UserData::class.java) }.getOrNull()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getUser(context) != null
    }

    fun clear(context: Context) {
        val preferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        preferences.edit().remove(KeyUserJson).apply()
    }
}
