package com.simats.scanmypills

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.simats.scanmypills.databinding.ActivityScanPillBinding

@Suppress("DEPRECATION")
class ScanPillActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanPillBinding
    private var frontImageUri: Uri? = null
    private var backImageUri: Uri? = null

    private val selectFrontImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            frontImageUri = it
            binding.ivFront.setImageURI(it)
            binding.ivFront.visibility = View.VISIBLE
            binding.layoutFrontPlaceholder.visibility = View.GONE
            updateProcessButtonState()
        }
    }

    private val selectBackImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            backImageUri = it
            binding.ivBack.setImageURI(it)
            binding.ivBack.visibility = View.VISIBLE
            binding.layoutBackPlaceholder.visibility = View.GONE
            updateProcessButtonState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanPillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.cardFront.setOnClickListener {
            selectFrontImage.launch("image/*")
        }

        binding.cardBack.setOnClickListener {
            selectBackImage.launch("image/*")
        }

        binding.btnProcess.setOnClickListener {
            val intent = Intent(this, ScanReviewActivity::class.java)
            intent.putExtra("front_image_uri", frontImageUri.toString())
            intent.putExtra("back_image_uri", backImageUri.toString())
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        
        updateProcessButtonState()
        loadNavIcons()
        setupNavbar()
    }

        private fun loadNavIcons() {
        binding.ivNavHome.setImageResource(R.drawable.ic_home)
        binding.ivNavScan.setImageResource(R.drawable.ic_barcode_scanner)
        binding.ivNavIdentify.setImageResource(R.drawable.ic_camera)
        binding.ivNavReminders.setImageResource(R.drawable.ic_bell)
        binding.ivNavProfile.setImageResource(R.drawable.ic_settings)
    }

    private fun setupNavbar() {
        binding.navHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        
        binding.navIdentify.setOnClickListener {
            startActivity(Intent(this, IdentifyPillActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.navReminders.setOnClickListener {
            startActivity(Intent(this, RemindersActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.navSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun updateProcessButtonState() {
        val isBothSelected = frontImageUri != null && backImageUri != null
        binding.btnProcess.isEnabled = isBothSelected
        
        if (isBothSelected) {
            binding.btnProcess.backgroundTintList = android.content.res.ColorStateList.valueOf(
                getColor(R.color.btn_blue)
            )
        } else {
            binding.btnProcess.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#C7D2FE") // Light blue/gray
            )
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
