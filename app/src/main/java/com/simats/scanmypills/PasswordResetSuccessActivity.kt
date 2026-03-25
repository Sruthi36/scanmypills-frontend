package com.simats.scanmypills

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.simats.scanmypills.databinding.ActivityPasswordResetSuccessBinding

@Suppress("DEPRECATION")
class PasswordResetSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordResetSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordResetSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }


    private fun setupListeners() {
        binding.btnGotoLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
