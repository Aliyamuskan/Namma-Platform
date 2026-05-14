package com.nammaplatform.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.nammaplatform.app.R
import com.nammaplatform.app.adapters.OnboardingAdapter
import com.nammaplatform.app.databinding.ActivityOnboardingBinding
import com.nammaplatform.app.models.OnboardingItem

class OnboardingActivity : BaseActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val onboardingComplete = sharedPref.getBoolean("onboarding_complete", false)
        
        val settingsPref = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val languageSelected = settingsPref.getString("language", null) != null

        // Check flow: Splash -> Language -> Onboarding -> Permissions -> Home
        if (onboardingComplete) {
            if (!languageSelected) {
                navigateToLanguageSelection()
                return
            } else {
                navigateToHome()
                return
            }
        }

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()

        binding.btnSkip.setOnClickListener {
            completeOnboarding()
        }

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem + 1 < adapter.itemCount) {
                binding.viewPager.currentItem += 1
            } else {
                completeOnboarding()
            }
        }
    }

    private fun setupViewPager() {
        val onboardingItems = listOf(
            OnboardingItem(
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1),
                R.drawable.ic_launcher_foreground
            ),
            OnboardingItem(
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2),
                R.drawable.ic_launcher_foreground
            ),
            OnboardingItem(
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3),
                R.drawable.ic_launcher_foreground
            )
        )

        adapter = OnboardingAdapter(onboardingItems)
        binding.viewPager.adapter = adapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == adapter.itemCount - 1) {
                    binding.btnNext.text = getString(R.string.get_started)
                } else {
                    binding.btnNext.text = getString(R.string.next)
                }
            }
        })
    }

    private fun completeOnboarding() {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("onboarding_complete", true)
            apply()
        }
        navigateToHome()
    }

    private fun navigateToLanguageSelection() {
        startActivity(Intent(this, LanguageSelectionActivity::class.java))
        finish()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
