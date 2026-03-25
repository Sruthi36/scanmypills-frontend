package com.simats.scanmypills

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.scanmypills.databinding.ActivityOnboarding3Binding
import com.bumptech.glide.Glide

@Suppress("DEPRECATION")
class Onboarding3Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboarding3Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboarding3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        loadIllustration()
    }

    private fun loadIllustration() {
        val imageUrl = "https://image2url.com/r2/default/images/1773385212269-163c9287-cba1-4170-831d-807dbaa3f22d.png" // Safe. Organized. In control.
        Glide.with(this)
            .load(imageUrl)
            .into(binding.ivIllustration)
    }

    private fun setupListeners() {
        binding.btnNext.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
    }
}
