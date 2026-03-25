package com.simats.scanmypills

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.scanmypills.databinding.ActivityPrivacyPolicyBinding

@Suppress("DEPRECATION")
class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupListeners() {
        binding.btnBackToSettings.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
