package com.nammaplatform.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.nammaplatform.app.databinding.ActivityLanguageSelectionBinding
import com.nammaplatform.app.util.LocaleHelper

class LanguageSelectionActivity : BaseActivity() {

    private lateinit var binding: ActivityLanguageSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Ensure high-contrast buttons are clearly clickable
        binding.btnKannada.setOnClickListener { saveLanguageAndNavigate("kn") }
        binding.btnEnglish.setOnClickListener { saveLanguageAndNavigate("en") }
        binding.btnHindi.setOnClickListener { saveLanguageAndNavigate("hi") }
    }

    private fun saveLanguageAndNavigate(langCode: String) {
        val sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE)
        sharedPref.edit().putString("language", langCode).apply()
        
        // Update current locale to reflect changes immediately
        LocaleHelper.setLocale(this, langCode)
        
        // Check if onboarding is complete to decide where to go
        val appPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val onboardingComplete = appPref.getBoolean("onboarding_complete", false)
        
        if (!onboardingComplete) {
            startActivity(Intent(this, OnboardingActivity::class.java))
        } else {
            // If coming from Settings/Home, just go back to Home
            startActivity(Intent(this, HomeActivity::class.java))
        }
        finish()
    }
}
