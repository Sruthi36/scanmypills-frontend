package com.simats.scanmypills

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.scanmypills.databinding.ActivityAccountCreatedBinding

@Suppress("DEPRECATION")
class AccountCreatedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountCreatedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountCreatedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnContinue.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
