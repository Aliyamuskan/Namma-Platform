package com.nammaplatform.app.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.nammaplatform.app.adapters.FacilityAdapter
import com.nammaplatform.app.databinding.ActivityStationFacilitiesBinding
import com.nammaplatform.app.models.FacilityModel

class StationFacilitiesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStationFacilitiesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStationFacilitiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val facilities = listOf(
            FacilityModel("Washroom", android.R.drawable.ic_menu_myplaces),
            FacilityModel("Drinking Water", android.R.drawable.ic_menu_send),
            FacilityModel("Food Stall", android.R.drawable.ic_menu_view),
            FacilityModel("Waiting Hall", android.R.drawable.ic_menu_slideshow),
            FacilityModel("Ticket Counter", android.R.drawable.ic_menu_agenda),
            FacilityModel("Parking", android.R.drawable.ic_menu_mapmode)
        )

        binding.rvFacilities.layoutManager = GridLayoutManager(this, 2)
        binding.rvFacilities.adapter = FacilityAdapter(facilities)
    }
}