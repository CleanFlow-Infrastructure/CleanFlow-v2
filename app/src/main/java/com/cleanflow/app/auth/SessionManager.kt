package com.cleanflow.app.auth

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("CleanFlowPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val DEFAULT_USER_ID = "user_demo_101"
        private const val DEFAULT_USER_EMAIL = "sipho.ndlovu@cleanflow.org"
    }

    fun saveSession(userId: String, email: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            apply()
        }
    }

    fun getUserId(): String = prefs.getString(KEY_USER_ID, DEFAULT_USER_ID) ?: DEFAULT_USER_ID

    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, DEFAULT_USER_EMAIL) ?: DEFAULT_USER_EMAIL

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.contains(KEY_USER_ID)
}