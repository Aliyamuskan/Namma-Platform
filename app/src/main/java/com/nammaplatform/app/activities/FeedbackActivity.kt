package com.nammaplatform.app.activities

import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.nammaplatform.app.R
import com.nammaplatform.app.databinding.ActivityFeedbackBinding

class FeedbackActivity : BaseActivity() {

    private lateinit var binding: ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnSubmitFeedback.setOnClickListener {
            val rating = binding.ratingBar.rating
            val text = binding.etFeedback.text.toString()

            if (text.isNotEmpty()) {
                saveFeedbackToFirebase(rating, text)
            } else {
                Toast.makeText(this, getString(R.string.no_results), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveFeedbackToFirebase(rating: Float, text: String) {
        val database = FirebaseDatabase.getInstance().getReference("feedback")
        val feedbackId = database.push().key ?: return
        
        val feedbackMap = mapOf(
            "rating" to rating,
            "feedbackText" to text,
            "timestamp" to System.currentTimeMillis()
        )

        database.child(feedbackId).setValue(feedbackMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show()
            }
    }
}
