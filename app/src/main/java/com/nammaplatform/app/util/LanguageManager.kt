package com.nammaplatform.app.util

import android.content.Context
import android.content.SharedPreferences

class LanguageManager(private val context: Context) {
    private val sharedPref: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun setLanguage(langCode: String) {
        sharedPref.edit().putString("language", langCode).apply()
        LocaleHelper.setLocale(context, langCode)
    }

    fun getLanguage(): String {
        return sharedPref.getString("language", "en") ?: "en"
    }

    companion object {
        fun getLanguage(context: Context): String {
            return context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getString("language", "en") ?: "en"
        }
    }
}
