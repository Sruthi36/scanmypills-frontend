package com.simats.scanmypills

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.scanmypills.databinding.ActivityOnboarding2Binding
import com.bumptech.glide.Glide

@Suppress("DEPRECATION")
class Onboarding2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboarding2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboarding2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        loadIllustration()
    }

    private fun loadIllustration() {
        val imageUrl = "https://image2url.com/r2/default/images/1773385372436-352b3182-4a77-477d-bd84-e86701ff5bdc.png" // Stay on track effortlessly
        Glide.with(this)
            .load(imageUrl)
            .into(binding.ivIllustration)
    }

    private fun setupListeners() {
        binding.btnNext.setOnClickListener {
            val intent = Intent(this, Onboarding3Activity::class.java)
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
