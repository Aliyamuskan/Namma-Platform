package com.nammaplatform.app.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        
        return context.createConfigurationContext(configuration)
    }

    fun onAttach(context: Context): Context {
        val lang = getPersistedData(context, Locale.getDefault().language)
        return setLocale(context, lang)
    }

    private fun getPersistedData(context: Context, defaultLanguage: String): String {
        val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return sharedPref.getString("language", defaultLanguage) ?: defaultLanguage
    }
}
