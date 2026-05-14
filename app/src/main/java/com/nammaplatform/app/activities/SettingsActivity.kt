package com.nammaplatform.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.nammaplatform.app.databinding.ActivitySettingsBinding

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupSettings()
    }

    private fun setupSettings() {
        val sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE)
        
        // Dark Mode
        binding.switchDarkMode.isChecked = sharedPref.getBoolean("dark_mode", false)
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("dark_mode", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Language
        binding.btnChangeLanguage.setOnClickListener {
            startActivity(Intent(this, LanguageSelectionActivity::class.java))
        }

        // Voice Speed
        val voiceSpeed = sharedPref.getFloat("voice_speed", 1.0f)
        binding.sliderVoiceSpeed.value = voiceSpeed
        binding.sliderVoiceSpeed.addOnChangeListener { _, value, _ ->
            sharedPref.edit().putFloat("voice_speed", value).apply()
        }
    }
}
