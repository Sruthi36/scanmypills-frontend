package com.simats.scanmypills

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.simats.scanmypills.databinding.ActivityIdentifyResultBinding
import com.simats.scanmypills.databinding.ItemIdentifyMatchBinding
import com.simats.scanmypills.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Toast
import android.widget.TextView
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity

@Suppress("DEPRECATION")
class IdentifyResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIdentifyResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdentifyResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Get captured image and name
        val imageUriString = intent.getStringExtra("tablet_image_uri")
        imageUriString?.let {
            val uri = Uri.parse(it)
            Glide.with(this).load(uri).into(binding.ivCapturedTablet)
        }

        val enteredName = intent.getStringExtra("medicine_name") ?: "Unknown Medicine"
        binding.tvEnteredMedicineName.text = enteredName

        // Identify medicine from API
        performIdentification(enteredName)

        // Scan another
        binding.btnScanAnother.setOnClickListener {
            val intent = Intent(this, IdentifyPillActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        loadNavIcons()
        setupNavbar()
    }

    private fun performIdentification(name: String) {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val token = prefs.getString("auth_token", null) ?: return

        RetrofitClient.instance.identifyMedicine("Bearer $token", name)
            .enqueue(object : Callback<IdentifyMedicineResponse> {
                override fun onResponse(call: Call<IdentifyMedicineResponse>, response: Response<IdentifyMedicineResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val result = response.body()!!
                        updateMatchesUI(result.matches, result.suggestions)
                    } else {
                        Toast.makeText(this@IdentifyResultActivity, "Identification failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<IdentifyMedicineResponse>, t: Throwable) {
                    Toast.makeText(this@IdentifyResultActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateMatchesUI(matches: List<IdentifyMatch>, suggestions: List<String>) {
        binding.tvMatchCount.text = "Found ${matches.size} matches"
        binding.layoutMatches.removeAllViews()

        if (matches.isEmpty()) {
            if (suggestions.isNotEmpty()) {
                showSuggestions(suggestions)
            } else {
                val tvNoResults = TextView(this).apply {
                    text = "No matches or suggestions found."
                    gravity = Gravity.CENTER
                    setPadding(0, 40, 0, 40)
                    setTextColor(Color.GRAY)
                }
                binding.layoutMatches.addView(tvNoResults)
            }
            return
        }

        matches.forEach { match ->
            val matchBinding = ItemIdentifyMatchBinding.inflate(LayoutInflater.from(this), binding.layoutMatches, false)
            matchBinding.tvMatchName.text = match.name
            matchBinding.tvMatchManufacturer.text = match.manufacturer ?: "Unknown"
            
            Glide.with(this)
                .load(RetrofitClient.getImageUrl(match.mainImage))
                .placeholder(R.drawable.pill_placeholder)
                .circleCrop()
                .into(matchBinding.ivMatchPill)

            matchBinding.root.setOnClickListener {
                val intent = Intent(this, MedicineDetailsActivity::class.java).apply {
                    putExtra("medicine_id", match.id)
                    putExtra("medicine_name", match.name)
                    putExtra("manufacturer", match.manufacturer)
                    putExtra("dosage", match.dosage)
                    putExtra("expiry_date", match.expiryDate)
                    putExtra("category", match.category)
                    putExtra("main_image_uri", match.mainImage)
                    putExtra("front_image_uri", match.frontImage)
                    putExtra("back_image_uri", match.backImage)
                }
                startActivity(intent)
            }

            binding.layoutMatches.addView(matchBinding.root)
        }
    }

    private fun showSuggestions(suggestions: List<String>) {
        val tvHeader = TextView(this).apply {
            text = "Did you mean?"
            textSize = 18f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 16)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        binding.layoutMatches.addView(tvHeader)

        suggestions.forEach { suggestion ->
            val tvSuggestion = TextView(this).apply {
                text = suggestion
                textSize = 16f
                setTextColor(getColor(R.color.btn_blue))
                setPadding(16, 24, 16, 24)
                isClickable = true
                isFocusable = true
                val outValue = TypedValue()
                theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                setBackgroundResource(outValue.resourceId)
                
                setOnClickListener {
                    binding.tvEnteredMedicineName.text = suggestion
                    performIdentification(suggestion)
                }
            }
            binding.layoutMatches.addView(tvSuggestion)
        }
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
        }
        binding.navScan.setOnClickListener {
            val intent = Intent(this, ScanPillActivity::class.java)
            startActivity(intent)
        }
        binding.navIdentify.setOnClickListener { } // Active
        binding.navReminders.setOnClickListener {
            val intent = Intent(this, RemindersActivity::class.java)
            startActivity(intent)
        }
        binding.navProfile.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

}
