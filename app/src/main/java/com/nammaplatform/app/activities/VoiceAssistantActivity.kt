package com.nammaplatform.app.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.nammaplatform.app.R
import com.nammaplatform.app.databinding.ActivityVoiceAssistantBinding
import com.nammaplatform.app.models.TrainInfo
import com.nammaplatform.app.repository.TrainRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.*

class VoiceAssistantActivity : BaseActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityVoiceAssistantBinding
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var trainRepository: TrainRepository
    private val TAG = "VoiceAssistant"
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    private var lastAnnouncement: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceAssistantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        trainRepository = TrainRepository(this)
        tts = TextToSpeech(this, this)
        initSpeechRecognizer()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupUI()
        loadInitialData()
    }

    private fun setupUI() {
        binding.fabSpeak.setOnClickListener {
            if (checkAudioPermission()) {
                speakPrompt()
            }
        }

        binding.btnStop.setOnClickListener {
            stopAll()
        }

        binding.btnRepeat.setOnClickListener {
            val announcement = lastAnnouncement
            if (announcement != null) {
                binding.tvStatus.text = getString(R.string.hear_announcement)
                tts?.speak(announcement, TextToSpeech.QUEUE_FLUSH, null, "RepeatOnly")
            } else {
                val noMsg = getString(R.string.no_announcement_to_repeat)
                tts?.speak(noMsg, TextToSpeech.QUEUE_FLUSH, null, "NoAnnouncement")
                Toast.makeText(this, noMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadInitialData() {
        val sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedStation = sharedPref.getString("selected_station", "Bengaluru") ?: "Bengaluru"
        
        binding.tvCurrentStation.text = savedStation
        
        lifecycleScope.launch {
            trainRepository.getLiveTrains(savedStation).collectLatest { trains ->
                if (trains.isNotEmpty()) {
                    val firstTrain = trains[0]
                    binding.tvNextTrain.text = "${firstTrain.name} (${firstTrain.number})"
                    binding.tvPlatform.text = firstTrain.platform
                } else {
                    binding.tvNextTrain.text = getString(R.string.no_results)
                    binding.tvPlatform.text = getString(R.string.double_dash_placeholder)
                }
            }
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    binding.tvStatus.text = getString(R.string.voice_status_listening)
                    togglePulseAnimation(true)
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                
                override fun onEndOfSpeech() {
                    binding.tvStatus.text = getString(R.string.voice_status_processing)
                    togglePulseAnimation(false)
                }

                override fun onError(error: Int) {
                    binding.tvStatus.text = getString(R.string.voice_status_tap_to_listen)
                    togglePulseAnimation(false)
                    Log.e(TAG, "Speech Recognizer Error: $error")
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val topMatch = matches[0]
                        binding.tvStatus.text = getString(R.string.voice_status_you_said, topMatch)
                        processVoiceQueries(matches)
                    }
                    togglePulseAnimation(false)
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    private fun togglePulseAnimation(show: Boolean) {
        if (show) {
            binding.pulseOuter.visibility = View.VISIBLE
            binding.pulseInner.visibility = View.VISIBLE
            
            val pulse = ScaleAnimation(
                1f, 1.2f, 1f, 1.2f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 800
                repeatMode = Animation.REVERSE
                repeatCount = Animation.INFINITE
            }
            
            binding.pulseOuter.startAnimation(pulse)
            binding.pulseInner.startAnimation(pulse)
        } else {
            binding.pulseOuter.clearAnimation()
            binding.pulseInner.clearAnimation()
            binding.pulseOuter.visibility = View.INVISIBLE
            binding.pulseInner.visibility = View.INVISIBLE
        }
    }

    private fun stopAll() {
        tts?.stop()
        speechRecognizer?.stopListening()
        togglePulseAnimation(false)
        binding.tvStatus.text = getString(R.string.voice_status_tap_to_listen)
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            val lang = getSharedPreferences("settings", MODE_PRIVATE).getString("language", "kn") ?: "kn"
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (lang == "kn") "kn-IN" else if (lang == "hi") "hi-IN" else "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }
        runOnUiThread {
            speechRecognizer?.startListening(intent)
        }
    }

    private fun processVoiceQueries(queries: List<String>) {
        lifecycleScope.launch {
            val trains = trainRepository.getLiveTrains("").firstOrNull() ?: emptyList()
            var foundTrain: TrainInfo? = null
            
            // Try matching each recognition result
            for (query in queries) {
                foundTrain = findTrainInList(query, trains)
                if (foundTrain != null) break
            }

            if (foundTrain != null) {
                announceTrainDetails(foundTrain)
            } else {
                val errorMsg = getString(R.string.voice_error_no_found, queries[0])
                lastAnnouncement = errorMsg
                tts?.speak(errorMsg, TextToSpeech.QUEUE_FLUSH, null, "ErrorResult")
            }
        }
    }

    private fun findTrainInList(query: String, trains: List<TrainInfo>): TrainInfo? {
        val lang = getSharedPreferences("settings", MODE_PRIVATE).getString("language", "kn") ?: "kn"
        
        // 1. Normalize Input
        var cleanQuery = query.lowercase().trim().replace("[.,!?;]".toRegex(), "")
        
        if (lang == "kn") {
            // Remove common Kannada stop words
            val stopWords = listOf("ರೈಲು", "ಸಂಖ್ಯೆ", "ಬಗ್ಗೆ", "ಮಾಹಿತಿ", "ಹೇಳು", "ತಿಳಿಸು", "ಎಷ್ಟು", "ಯಾವಾಗ", "ಪ್ಲಾಟ್‌ಫಾರ್ಮ್")
            for (word in stopWords) {
                cleanQuery = cleanQuery.replace(word, "")
            }
            
            // Map Kannada word-numbers to digits
            val wordToDigit = mapOf(
                "ಸೊನ್ನೆ" to "0", "ಒಂದು" to "1", "ಎರಡು" to "2", "ಮೂರು" to "3", "ನಾಲ್ಕು" to "4",
                "ಐದು" to "5", "ಆರು" to "6", "ಏಳು" to "7", "ಎಂಟು" to "8", "ಒಂಬತ್ತು" to "9"
            )
            for ((word, digit) in wordToDigit) {
                cleanQuery = cleanQuery.replace(word, digit)
            }

            // Map Kannada digits to Arabic digits
            val kannadaDigits = charArrayOf('೦', '೧', '೨', '೩', '೪', '೫', '೬', '೭', '೮', '೯')
            val arabicDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
            val sb = StringBuilder()
            for (c in cleanQuery) {
                val index = kannadaDigits.indexOf(c)
                if (index != -1) sb.append(arabicDigits[index]) else sb.append(c)
            }
            cleanQuery = sb.toString()
        } else {
            cleanQuery = cleanQuery.replace("train", "").replace("number", "")
        }
        
        cleanQuery = cleanQuery.trim()
        val queryOnlyDigits = cleanQuery.filter { it.isDigit() }
        val queryNoSpaces = cleanQuery.replace("\\s".toRegex(), "")

        Log.d(TAG, "Search Match - Lang: $lang, Raw: $query, Clean: $cleanQuery, Digits: $queryOnlyDigits")

        // 2. Match by Number (High Priority)
        if (queryOnlyDigits.isNotEmpty()) {
            val found = trains.find { 
                it.number == queryOnlyDigits || it.number.contains(queryOnlyDigits) || queryOnlyDigits.contains(it.number) 
            }
            if (found != null) return found
        }

        // 3. Match by Name
        return trains.find { train ->
            val trainNameLocal = train.name.lowercase().trim()
            val trainNameNoSpaces = trainNameLocal.replace("\\s".toRegex(), "")
            
            cleanQuery.contains(trainNameLocal) || 
            trainNameLocal.contains(cleanQuery) ||
            (queryNoSpaces.length > 3 && (queryNoSpaces.contains(trainNameNoSpaces) || trainNameNoSpaces.contains(queryNoSpaces)))
        }
    }

    private fun announceTrainDetails(train: TrainInfo) {
        val announcement = getString(
            R.string.train_announcement_template,
            train.name,
            train.number,
            train.platform,
            train.arrivalTime,
            train.departureTime
        )
        lastAnnouncement = announcement
        tts?.speak(announcement, TextToSpeech.QUEUE_FLUSH, null, "AnnouncementResult")
    }

    private fun speakPrompt() {
        val prompt = getString(R.string.voice_prompt)
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "AssistantPrompt")
        tts?.speak(prompt, TextToSpeech.QUEUE_FLUSH, params, "AssistantPrompt")
    }

    private fun checkAudioPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
            return false
        }
        return true
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val lang = getSharedPreferences("settings", MODE_PRIVATE).getString("language", "kn") ?: "kn"
            tts?.setLanguage(Locale(lang))
            
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    if (utteranceId == "AssistantPrompt") {
                        startListening()
                    }
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {}
            })
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
