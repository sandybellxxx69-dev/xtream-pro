package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("xtream_prefs", Context.MODE_PRIVATE)

    fun saveCredentials(serverUrl: String, user: String, pass: String) {
        prefs.edit().apply {
            putString("SERVER_URL", serverUrl.trimEnd('/'))
            putString("USERNAME", user)
            putString("PASSWORD", pass)
            putBoolean("IS_LOGGED_IN", true)
            apply()
        }
    }

    fun getServerUrl(): String? = prefs.getString("SERVER_URL", null)
    fun getUsername(): String? = prefs.getString("USERNAME", null)
    fun getPassword(): String? = prefs.getString("PASSWORD", null)
    fun isLoggedIn(): Boolean = prefs.getBoolean("IS_LOGGED_IN", false)

    fun logout() {
        prefs.edit().clear().apply()
    }
}
