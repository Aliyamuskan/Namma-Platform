package com.nammaplatform.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nammaplatform.app.R
import com.nammaplatform.app.adapters.TrainAdapter
import com.nammaplatform.app.databinding.ActivityHomeBinding
import com.nammaplatform.app.models.TrainInfo
import com.nammaplatform.app.repository.TrainRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class HomeActivity : BaseActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var trainRepository: TrainRepository
    private lateinit var trainAdapter: TrainAdapter
    private var tts: TextToSpeech? = null
    private var updatesJob: Job? = null
    
    private val stationList = listOf("Bengaluru", "Mysuru", "Hubballi", "Davangere", "Tumakuru")
    private val DEFAULT_STATION = "Bengaluru"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        trainRepository = TrainRepository(this)
        tts = TextToSpeech(this, this)

        setupToolbar()
        setupBottomNavigation()
        setupRecyclerView()
        setupStationSelector()
    }

    private fun setupToolbar() {
        binding.toolbarTitle.text = getString(R.string.app_name)
        
        binding.btnLanguage.setOnClickListener {
            startActivity(Intent(this, LanguageSelectionActivity::class.java))
        }
        
        binding.btnEmergency.setOnClickListener {
            showEmergencyConfirmation()
        }
        
        Log.d("HomeActivity", "Custom toolbar setup complete using direct view IDs")
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    binding.nestedScrollView.smoothScrollTo(0, 0)
                    true
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, TrainSearchActivity::class.java))
                    false 
                }
                R.id.nav_coach -> {
                    val intent = Intent(this, TrainSearchActivity::class.java)
                    intent.putExtra("IS_COACH_GUIDE_FLOW", true)
                    startActivity(intent)
                    false
                }
                R.id.nav_voice -> {
                    startActivity(Intent(this, VoiceAssistantActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        trainAdapter = TrainAdapter(
            trains = emptyList(),
            onCoachClick = { train -> openCoachLayout(train) },
            onSpeakClick = { train -> speakTrainDetails(train) },
            onDetailsClick = { train ->
                val intent = Intent(this@HomeActivity, TrainDetailsActivity::class.java)
                intent.putExtra("TRAIN_NUMBER", train.number)
                startActivity(intent)
            }
        )
        binding.rvUpcomingTrains.layoutManager = LinearLayoutManager(this)
        binding.rvUpcomingTrains.adapter = trainAdapter
    }

    private fun setupStationSelector() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, stationList)
        binding.actvStationSelector.setAdapter(adapter)

        val sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedStation = sharedPref.getString("selected_station", DEFAULT_STATION) ?: DEFAULT_STATION
        
        binding.actvStationSelector.setText(savedStation, false)
        loadUpcomingTrains(savedStation)

        binding.actvStationSelector.setOnItemClickListener { _, _, _, _ ->
            val selected = binding.actvStationSelector.text.toString()
            saveStation(selected)
            loadUpcomingTrains(selected)
        }
    }

    private fun saveStation(station: String) {
        getSharedPreferences("settings", Context.MODE_PRIVATE).edit().apply {
            putString("selected_station", station)
            apply()
        }
    }

    private fun loadUpcomingTrains(station: String) {
        updatesJob?.cancel()
        updatesJob = lifecycleScope.launch {
            trainRepository.getLiveTrains(station).collectLatest { trains ->
                // Project Requirement: Limit the station-based upcoming train display to only 3 trains on the dashboard
                val previewTrains = trains.take(3)
                trainAdapter.updateData(previewTrains)
            }
        }
    }

    private fun showEmergencyConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.emergency_label)
            .setMessage(R.string.emergency_confirm_msg)
            .setPositiveButton(R.string.next) { _, _ ->
                startActivity(Intent(this, EmergencyHelpActivity::class.java))
            }
            .setNegativeButton(R.string.skip, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun openCoachLayout(train: TrainInfo) {
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

    private fun speakTrainDetails(train: TrainInfo) {
        val announcement = getString(
            R.string.train_announcement_template,
            train.name,
            train.number,
            train.platform,
            train.arrivalTime,
            train.departureTime
        )
        tts?.speak(announcement, TextToSpeech.QUEUE_FLUSH, null, "TrainTTS")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val lang = getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getString("language", "kn") ?: "kn"
            tts?.setLanguage(Locale(lang))
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
