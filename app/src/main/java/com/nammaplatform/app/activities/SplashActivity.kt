package com.nammaplatform.app.activities

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import com.nammaplatform.app.databinding.ActivitySplashBinding

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startAnimations()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LanguageSelectionActivity::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 3000)
    }

    private fun startAnimations() {
        // 1. Center Content Fade-in and Scale-up
        binding.centerContent.alpha = 0f
        binding.centerContent.scaleX = 0.8f
        binding.centerContent.scaleY = 0.8f
        
        binding.centerContent.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1200)
            .setInterpolator(AnticipateOvershootInterpolator())
            .start()

        // 2. Animated Train crossing at the bottom
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val trainStartPos = -200f
        val trainEndPos = screenWidth + 200f
        
        binding.animatedTrain.translationX = trainStartPos
        
        val trainAnimator = ObjectAnimator.ofFloat(
            binding.animatedTrain,
            "translationX",
            trainStartPos,
            trainEndPos
        )
        trainAnimator.duration = 2500
        trainAnimator.startDelay = 500
        trainAnimator.interpolator = AccelerateDecelerateInterpolator()
        trainAnimator.start()

        // 3. Track lines subtle animation (fade in)
        binding.trackLine1.alpha = 0f
        binding.trackLine2.alpha = 0f
        
        binding.trackLine1.animate().alpha(0.2f).setDuration(1000).setStartDelay(300).start()
        binding.trackLine2.animate().alpha(0.2f).setDuration(1000).setStartDelay(500).start()
    }
}
