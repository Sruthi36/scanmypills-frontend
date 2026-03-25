package com.simats.scanmypills

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.scanmypills.databinding.ActivityDataStorageBinding

@Suppress("DEPRECATION")
class DataStorageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataStorageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataStorageBinding.inflate(layoutInflater)
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
        binding.btnClearData.setOnClickListener {
            Toast.makeText(this, "Clearing old data...", Toast.LENGTH_SHORT).show()
        }

        binding.btnDownloadJson.setOnClickListener {
            Toast.makeText(this, "Downloading JSON report...", Toast.LENGTH_SHORT).show()
        }

        binding.btnDownloadPdf.setOnClickListener {
            Toast.makeText(this, "Generating PDF report...", Toast.LENGTH_SHORT).show()
        }

        binding.btnBackToSettings.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
