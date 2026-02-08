package com.warehouse.upwely.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SettingsRepository {
    private const val PREFS_NAME = "upwely_api_settings"

    private const val KEY_BASE_URL = "base_url"
    private const val KEY_CLIENT_ID = "client_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"
    private const val KEY_CLIENT_SECRET = "client_secret"
    private const val KEY_ACCESS_TOKEN = "access_token"

    // Default values
    private const val DEFAULT_BASE_URL = "http://192.168.0.35/AcumaticaERP"
    private const val DEFAULT_CLIENT_ID = "D518FED4-997D-6E2C-1577-FC53E20CCEB2@Company"
    private const val DEFAULT_USERNAME = "admin"
    private const val DEFAULT_PASSWORD = "setup2"
    private const val DEFAULT_CLIENT_SECRET = "u0kz1WSHydzlwCmzpzdE3w"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getBaseUrl(context: Context): String {
        return getPrefs(context).getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    }

    fun setBaseUrl(context: Context, value: String) {
        getPrefs(context).edit { putString(KEY_BASE_URL, value) }
    }

    fun getClientId(context: Context): String {
        return getPrefs(context).getString(KEY_CLIENT_ID, DEFAULT_CLIENT_ID) ?: DEFAULT_CLIENT_ID
    }

    fun setClientId(context: Context, value: String) {
        getPrefs(context).edit { putString(KEY_CLIENT_ID, value) }
    }

    fun getUsername(context: Context): String {
        return getPrefs(context).getString(KEY_USERNAME, DEFAULT_USERNAME) ?: DEFAULT_USERNAME
    }

    fun setUsername(context: Context, value: String) {
        getPrefs(context).edit { putString(KEY_USERNAME, value) }
    }

    fun getPassword(context: Context): String {
        return getPrefs(context).getString(KEY_PASSWORD, DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD
    }

    fun setPassword(context: Context, value: String) {
        getPrefs(context).edit { putString(KEY_PASSWORD, value) }
    }

    fun getClientSecret(context: Context): String {
        return getPrefs(context).getString(KEY_CLIENT_SECRET, DEFAULT_CLIENT_SECRET) ?: DEFAULT_CLIENT_SECRET
    }

    fun setClientSecret(context: Context, value: String) {
        getPrefs(context).edit { putString(KEY_CLIENT_SECRET, value) }
    }

    fun getAccessToken(context: Context): String? {
        return getPrefs(context).getString(KEY_ACCESS_TOKEN, null)
    }

    fun setAccessToken(context: Context, value: String?) {
        getPrefs(context).edit {
            if (value != null) {
                putString(KEY_ACCESS_TOKEN, value)
            } else {
                remove(KEY_ACCESS_TOKEN)
            }
        }
    }

    fun clearAccessToken(context: Context) {
        setAccessToken(context, null)
    }
}
