package com.nammaplatform.app.activities

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.nammaplatform.app.R
import com.nammaplatform.app.databinding.ActivityTrainDetailsBinding
import com.nammaplatform.app.models.TrainInfo
import com.nammaplatform.app.repository.TrainRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class TrainDetailsActivity : BaseActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityTrainDetailsBinding
    private lateinit var trainRepository: TrainRepository
    private var tts: TextToSpeech? = null
    private var currentTrain: TrainInfo? = null
    private val TAG = "TrainDetailsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        trainRepository = TrainRepository(this)
        tts = TextToSpeech(this, this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val trainNumber = intent.getStringExtra("TRAIN_NUMBER")
        if (trainNumber != null) {
            loadTrainDetails(trainNumber)
        } else {
            finish()
        }

        binding.btnViewCoachLayout.setOnClickListener {
            currentTrain?.let { train ->
                val intent = Intent(this, CoachLayoutActivity::class.java).apply {
                    putExtra("train_name", train.name)
                    putExtra("train_number", train.number)
                    putExtra("platform", train.platform)
                    putExtra("arrival_time", train.arrivalTime)
                    putExtra("departure_time", train.departureTime)
                    putExtra("status", train.status)
                    putStringArrayListExtra("coaches_list", ArrayList(train.coaches))
                }
                startActivity(intent)
            }
        }

        binding.btnHearAnnouncement.setOnClickListener {
            speakAnnouncement()
        }

        binding.btnNavigate.setOnClickListener {
            val intent = Intent(this, PlatformNavigationActivity::class.java)
            intent.putExtra("PLATFORM", currentTrain?.platform)
            startActivity(intent)
        }
    }

    private fun loadTrainDetails(trainNumber: String) {
        lifecycleScope.launch {
            trainRepository.getTrainByNumber(trainNumber).collectLatest { train ->
                if (train != null) {
                    currentTrain = train
                    binding.apply {
                        tvTrainName.text = train.name
                        tvTrainNumber.text = train.number
                        tvPlatform.text = train.platform
                        tvTime.text = train.arrivalTime
                        tvDeparture.text = train.departureTime
                        tvStatus.text = train.status
                        tvDest.text = train.destination
                    }
                }
            }
        }
    }

    private fun speakAnnouncement() {
        currentTrain?.let { train ->
            val announcement = getString(
                R.string.train_announcement_template,
                train.name,
                train.number,
                train.platform,
                train.arrivalTime,
                train.departureTime
            )
            tts?.speak(announcement, TextToSpeech.QUEUE_FLUSH, null, "AnnouncementID")
        }
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
