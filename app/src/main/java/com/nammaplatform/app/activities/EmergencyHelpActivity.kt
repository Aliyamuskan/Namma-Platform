package com.nammaplatform.app.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.nammaplatform.app.databinding.ActivityEmergencyHelpBinding

class EmergencyHelpActivity : BaseActivity() {

    private lateinit var binding: ActivityEmergencyHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.cardPolice.setOnClickListener {
            makeCall("1512")
        }

        binding.cardMedical.setOnClickListener {
            makeCall("108")
        }

        binding.cardHelpLine.setOnClickListener {
            makeCall("139")
        }
    }

    private fun makeCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }
}
