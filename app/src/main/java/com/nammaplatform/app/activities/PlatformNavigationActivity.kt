package com.nammaplatform.app.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nammaplatform.app.databinding.ActivityPlatformNavigationBinding

class PlatformNavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlatformNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlatformNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
}