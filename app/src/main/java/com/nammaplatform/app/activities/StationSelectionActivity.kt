package com.nammaplatform.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.nammaplatform.app.adapters.StationAdapter
import com.nammaplatform.app.databinding.ActivityStationSelectionBinding

class StationSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStationSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStationSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        val stations = listOf("Bengaluru", "Mysuru", "Hubballi", "Davangere", "Tumakuru")
        
        val adapter = StationAdapter(stations) { selectedStation ->
            saveStation(selectedStation)
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
        }

        binding.rvStations.layoutManager = LinearLayoutManager(this)
        binding.rvStations.adapter = adapter
    }

    private fun saveStation(station: String) {
        val sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("selected_station", station)
            apply()
        }
    }
}
