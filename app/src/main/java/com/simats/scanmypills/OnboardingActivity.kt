package com.simats.scanmypills

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.scanmypills.databinding.ActivityOnboardingBinding
import com.bumptech.glide.Glide

@Suppress("DEPRECATION")
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        loadIllustration()
    }

    private fun loadIllustration() {
        val imageUrl = "https://image2url.com/r2/default/images/1773384899122-755e60e4-5f75-4406-bad9-069ebcf9c5f7.png" // Smart medicine scanning
        Glide.with(this)
            .load(imageUrl)
            .into(binding.ivIllustration)
    }

    private fun setupListeners() {
        binding.btnNext.setOnClickListener {
            val intent = Intent(this, Onboarding2Activity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }

        binding.btnSkip.setOnClickListener {
            val intent = Intent(this, Onboarding3Activity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
    }
}
