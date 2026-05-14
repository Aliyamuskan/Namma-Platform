package com.nammaplatform.app.activities

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.nammaplatform.app.R
import com.nammaplatform.app.databinding.ActivityMainBinding
import org.json.JSONArray
import java.util.Locale

class MainActivity : BaseActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding
    private var tts: TextToSpeech? = null
    private var trainDataList = mutableListOf<TrainData>()

    data class TrainData(
        val number: String,
        val name: String,
        val platform: String,
        val coaches: List<String>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Initialize TTS
        tts = TextToSpeech(this, this)

        // 2. Load and Parse JSON
        loadTrainData()

        // 3. Setup Dropdown (Selecting Train as per JSON data)
        setupTrainSelection()

        // 4. Help Me Button Logic
        binding.btnHelp.setOnClickListener {
            speakCurrentStatus()
        }
    }

    private fun loadTrainData() {
        try {
            val jsonString = assets.open("trains.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val coachesJson = obj.getJSONArray("coaches")
                val coachesList = mutableListOf<String>()
                for (j in 0 until coachesJson.length()) {
                    coachesList.add(coachesJson.getString(j))
                }
                trainDataList.add(
                    TrainData(
                        obj.getString("train_number"),
                        obj.getString("name_en"), // Default to English name for the list
                        obj.getString("platform"),
                        coachesList
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupTrainSelection() {
        val trainNames = trainDataList.map { "${it.number} - ${it.name}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, trainNames)
        binding.actvStation.setAdapter(adapter)

        binding.actvStation.setOnItemClickListener { _, _, position, _ ->
            val selectedTrain = trainDataList[position]
            updateUI(selectedTrain)
        }
    }

    private fun updateUI(train: TrainData) {
        binding.tvPlatformNumber.text = train.platform
        
        // Clear and populate coach container
        binding.llCoachContainer.removeAllViews()
        train.coaches.forEach { coachType ->
            addCoachView(coachType)
        }
    }

    private fun addCoachView(type: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_coach_visual, binding.llCoachContainer, false)
        val ivCoach = view.findViewById<ImageView>(R.id.ivCoachIcon)
        val tvLabel = view.findViewById<TextView>(R.id.tvCoachLabel)

        tvLabel.text = type
        when {
            type.contains("Engine", true) -> ivCoach.setImageResource(R.drawable.ic_coach_engine)
            type.contains("General", true) || type.startsWith("GS", true) -> ivCoach.setImageResource(R.drawable.ic_coach_general)
            else -> ivCoach.setImageResource(R.drawable.ic_coach_sleeper)
        }
        
        binding.llCoachContainer.addView(view)
    }

    private fun speakCurrentStatus() {
        val trainName = binding.actvStation.text.toString()
        val platform = binding.tvPlatformNumber.text.toString()
        
        if (platform == "--" || platform.isEmpty()) {
            val text = getString(R.string.select_train_first)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "StatusPrompt")
            return
        }

        val announcement = getString(R.string.train_arrival_announcement, trainName, platform)
        tts?.speak(announcement, TextToSpeech.QUEUE_FLUSH, null, "StatusAnnouncement")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val lang = getSharedPreferences("settings", MODE_PRIVATE).getString("language", "en") ?: "en"
            val locale = Locale(lang)
            tts?.setLanguage(locale)
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
