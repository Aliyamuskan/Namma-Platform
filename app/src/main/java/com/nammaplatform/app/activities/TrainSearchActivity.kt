package com.nammaplatform.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nammaplatform.app.R
import com.nammaplatform.app.adapters.TrainAdapter
import com.nammaplatform.app.databinding.ActivityTrainSearchBinding
import com.nammaplatform.app.models.TrainInfo
import com.nammaplatform.app.repository.TrainRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class TrainSearchActivity : BaseActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityTrainSearchBinding
    private lateinit var trainRepository: TrainRepository
    private lateinit var trainAdapter: TrainAdapter
    private var tts: TextToSpeech? = null
    private var isCoachGuideMode: Boolean = false
    private var allTrains: List<TrainInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isCoachGuideMode = intent.getBooleanExtra("IS_COACH_GUIDE_FLOW", false)
        trainRepository = TrainRepository(this)
        tts = TextToSpeech(this, this)

        setupToolbar()
        setupUIForMode()
        setupRecyclerView()
        setupSearchListener()
        
        loadTrains()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        supportActionBar?.title = if (isCoachGuideMode) {
            getString(R.string.next_arriving_trains)
        } else {
            getString(R.string.search_train_label)
        }
    }

    private fun setupUIForMode() {
        if (isCoachGuideMode) {
            binding.etSearch.visibility = View.GONE
            binding.tvGuidance.visibility = View.VISIBLE
        } else {
            binding.etSearch.visibility = View.VISIBLE
            binding.tvGuidance.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        trainAdapter = TrainAdapter(
            trains = emptyList(),
            isCoachGuideMode = isCoachGuideMode,
            onCoachClick = { train -> openCoachLayout(train) },
            onSpeakClick = { train -> speakTrainDetails(train) },
            onDetailsClick = { train ->
                val intent = Intent(this, TrainDetailsActivity::class.java)
                intent.putExtra("TRAIN_NUMBER", train.number)
                startActivity(intent)
            }
        )
        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(this@TrainSearchActivity)
            adapter = trainAdapter
        }
    }

    private fun setupSearchListener() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterTrains(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterTrains(query: String) {
        val filtered = if (query.isEmpty()) {
            allTrains
        } else {
            allTrains.filter { 
                it.number.contains(query, ignoreCase = true) || 
                it.name.contains(query, ignoreCase = true) 
            }
        }
        trainAdapter.updateData(filtered)
        binding.tvNoResults.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadTrains() {
        // For searching, we fetch all trains across all stations by passing an empty string
        lifecycleScope.launch {
            trainRepository.getLiveTrains("").collectLatest { trains ->
                allTrains = trains
                filterTrains(binding.etSearch.text.toString())
            }
        }
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

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
