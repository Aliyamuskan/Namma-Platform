package com.nammaplatform.app.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.nammaplatform.app.R
import com.nammaplatform.app.databinding.ActivityCoachLayoutBinding
import java.util.Locale

class CoachLayoutActivity : BaseActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityCoachLayoutBinding
    private var tts: TextToSpeech? = null
    private val TAG = "CoachLayoutActivity"

    private var trainName: String? = null
    private var trainNumber: String? = null
    private var platform: String? = null
    private var arrivalTime: String? = null
    private var departureTime: String? = null
    private var status: String? = null
    private var coaches: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoachLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receiveIntentExtras()
        tts = TextToSpeech(this, this)
        setupToolbar()
        bindTrainData()
        renderDynamicCoachLayout()
        setupClickListeners()
        
        startScrollingAnimation()
    }

    private fun receiveIntentExtras() {
        trainName = intent.getStringExtra("train_name")
        trainNumber = intent.getStringExtra("train_number")
        platform = intent.getStringExtra("platform")
        arrivalTime = intent.getStringExtra("arrival_time")
        departureTime = intent.getStringExtra("departure_time")
        status = intent.getStringExtra("status")
        coaches = intent.getStringArrayListExtra("coaches_list") ?: emptyList()
    }

    private fun bindTrainData() {
        binding.tvTrainName.text = trainName ?: getString(R.string.not_available_time)
        binding.tvTrainNumber.text = trainNumber ?: getString(R.string.not_available_time)
        
        binding.tvPlatform.text = getString(R.string.platform_display, platform ?: getString(R.string.not_available_time))
        binding.tvArrival.text = getString(R.string.arrival_display, arrivalTime ?: getString(R.string.not_available_time))
        binding.tvDeparture.text = getString(R.string.departure_display, departureTime ?: getString(R.string.not_available_time))
        binding.tvStatus.text = status ?: ""

        val statusColor = if (status?.lowercase()?.contains("on time") == true || status?.contains("ಸರಿಯಾದ") == true) {
            ContextCompat.getColor(this, R.color.status_on_time)
        } else {
            ContextCompat.getColor(this, R.color.status_delayed)
        }
        binding.tvStatus.setTextColor(statusColor)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun renderDynamicCoachLayout() {
        binding.llCoachContainer.removeAllViews()
        if (coaches.isEmpty()) return

        coaches.forEachIndexed { index, coachType ->
            val view = renderCoachView(coachType)
            binding.llCoachContainer.addView(view)
            
            view.alpha = 0f
            view.translationX = 100f
            view.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(300)
                .setStartDelay(index * 100L)
                .start()
        }
    }

    private fun renderCoachView(coachType: String): View {
        val columnLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val coachView = LayoutInflater.from(this).inflate(R.layout.item_coach_visual, columnLayout, false)
        val ivIcon = coachView.findViewById<ImageView>(R.id.ivCoachIcon)
        val tvLabel = coachView.findViewById<TextView>(R.id.tvCoachLabel)
        val cardBody = coachView.findViewById<MaterialCardView>(R.id.cardCoachBody)

        tvLabel.text = coachType
        val typeUpper = coachType.uppercase()
        when {
            typeUpper.contains("ENG") -> {
                ivIcon.setImageResource(R.drawable.ic_coach_engine)
                cardBody.setCardBackgroundColor(ContextCompat.getColor(this, R.color.coach_engine))
            }
            typeUpper.contains("GEN") -> {
                ivIcon.setImageResource(R.drawable.ic_coach_general)
                cardBody.setCardBackgroundColor(ContextCompat.getColor(this, R.color.coach_general))
            }
            else -> {
                ivIcon.setImageResource(R.drawable.ic_coach_sleeper)
                cardBody.setCardBackgroundColor(ContextCompat.getColor(this, R.color.coach_sleeper))
            }
        }
        columnLayout.addView(coachView)
        return columnLayout
    }

    private fun startScrollingAnimation() {
        Handler(Looper.getMainLooper()).postDelayed({
            binding.hsvCoachLayout.fullScroll(View.FOCUS_RIGHT)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.hsvCoachLayout.smoothScrollTo(0, 0)
            }, 1000)
        }, 500)
    }

    private fun setupClickListeners() {
        binding.btnSpeakKannada.setOnClickListener { speakAnnouncement() }
        binding.btnPlatformNav.setOnClickListener {
            val intent = Intent(this, PlatformNavigationActivity::class.java)
            intent.putExtra("PLATFORM", platform)
            startActivity(intent)
        }
        binding.btnBackHome.setOnClickListener { finish() }
    }

    private fun speakAnnouncement() {
        // Fixed: Use localized template instead of hardcoded Kannada string
        val announcement = getString(
            R.string.train_announcement_template,
            trainName ?: "",
            trainNumber ?: "",
            platform ?: "",
            arrivalTime ?: "",
            departureTime ?: ""
        )
        tts?.speak(announcement, TextToSpeech.QUEUE_FLUSH, null, "CoachAnnouncement")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val lang = getSharedPreferences("settings", MODE_PRIVATE).getString("language", "kn") ?: "kn"
            tts?.setLanguage(Locale(lang))
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
