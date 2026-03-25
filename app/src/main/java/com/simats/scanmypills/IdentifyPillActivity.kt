package com.simats.scanmypills

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.simats.scanmypills.databinding.ActivityIdentifyPillBinding

@Suppress("DEPRECATION")
class IdentifyPillActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIdentifyPillBinding
    private var tabletImageUri: Uri? = null

    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            tabletImageUri = it
            binding.ivTablet.setImageURI(it)
            binding.ivTablet.visibility = View.VISIBLE
            binding.layoutPlaceholder.visibility = View.GONE
            updateButtonState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdentifyPillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar setup
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Capture click
        binding.cardTablet.setOnClickListener {
            selectImage.launch("image/*")
        }

        // Medicine Name TextWatcher
        binding.etMedicineName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Identify button click
        binding.btnIdentify.setOnClickListener {
            val intent = Intent(this, IdentifyResultActivity::class.java)
            intent.putExtra("tablet_image_uri", tabletImageUri.toString())
            intent.putExtra("medicine_name", binding.etMedicineName.text.toString().trim())
            startActivity(intent)
        }

        loadNavIcons()
        setupNavbar()
        updateButtonState()
    }

        private fun loadNavIcons() {
        binding.ivNavHome.setImageResource(R.drawable.ic_home)
        binding.ivNavScan.setImageResource(R.drawable.ic_barcode_scanner)
        binding.ivNavIdentify.setImageResource(R.drawable.ic_camera)
        binding.ivNavReminders.setImageResource(R.drawable.ic_bell)
        binding.ivNavSettings.setImageResource(R.drawable.ic_settings)
    }

    private fun setupNavbar() {
        binding.navHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        binding.navScan.setOnClickListener {
            val intent = Intent(this, ScanPillActivity::class.java)
            startActivity(intent)
        }

        // Identify is active
        binding.navIdentify.setOnClickListener { }

        binding.navReminders.setOnClickListener {
            val intent = Intent(this, RemindersActivity::class.java)
            startActivity(intent)
        }

        binding.navSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateButtonState() {
        val isImageSelected = tabletImageUri != null
        val isNameEntered = binding.etMedicineName.text.toString().trim().isNotEmpty()
        val isEnabled = isImageSelected && isNameEntered
        
        binding.btnIdentify.isEnabled = isEnabled
        
        if (isEnabled) {
            binding.btnIdentify.backgroundTintList = android.content.res.ColorStateList.valueOf(
                getColor(R.color.btn_blue)
            )
        } else {
            binding.btnIdentify.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#C7D2FE") // Light blue/gray
            )
        }
    }
}
