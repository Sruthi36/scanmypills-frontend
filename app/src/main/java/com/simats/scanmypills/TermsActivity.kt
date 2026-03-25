package com.simats.scanmypills

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.simats.scanmypills.databinding.ActivityTermsBinding

@Suppress("DEPRECATION")
class TermsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTermsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fromSignup = intent.getBooleanExtra("from_signup", false)

        setupToolbar()
        
        if (fromSignup) {
            binding.btnBack.visibility = View.GONE
            binding.layoutSignUpButtons.visibility = View.VISIBLE
        } else {
            binding.btnBack.visibility = View.VISIBLE
            binding.layoutSignUpButtons.visibility = View.GONE
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnAccept.setOnClickListener {
            // Store acceptance if needed
            val intent = Intent(this, AccountCreatedActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnDecline.setOnClickListener {
            finish() // Simply go back or handle as rejection
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
