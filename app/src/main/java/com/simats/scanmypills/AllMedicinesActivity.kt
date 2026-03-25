package com.simats.scanmypills

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.simats.scanmypills.databinding.ActivityAllMedicinesBinding
import com.simats.scanmypills.databinding.ItemMedicineCardBinding
import com.simats.scanmypills.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Toast
import android.content.Context

@Suppress("DEPRECATION")
class AllMedicinesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllMedicinesBinding
    private var allMedicines: List<MedicineData> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllMedicinesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Add button
        binding.btnAddMedicine.setOnClickListener {
            val intent = Intent(this, ScanPillActivity::class.java)
            startActivity(intent)
        }

        loadNavIcons()
        setupNavbar()
        setupSearch()
    }

    override fun onResume() {
        super.onResume()
        fetchAllMedicines()
    }

    private fun fetchAllMedicines() {
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null) ?: return

        RetrofitClient.instance.getAllMedicines("Bearer $token")
            .enqueue(object : Callback<AllMedicinesResponse> {
                override fun onResponse(call: Call<AllMedicinesResponse>, response: Response<AllMedicinesResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        allMedicines = response.body()?.medicines ?: emptyList()
                        updateListUI(allMedicines)
                    } else {
                        Toast.makeText(this@AllMedicinesActivity, "Failed to load medicines", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AllMedicinesResponse>, t: Throwable) {
                    Toast.makeText(this@AllMedicinesActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMedicines(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun filterMedicines(query: String) {
        if (query.isEmpty()) {
            updateListUI(allMedicines)
            return
        }

        val filtered = allMedicines.filter { 
            (it.name?.contains(query, ignoreCase = true) == true) || 
            (it.manufacturer?.contains(query, ignoreCase = true) == true)
        }
        updateListUI(filtered, isFiltering = true)
    }

    private fun updateListUI(medicines: List<MedicineData>, isFiltering: Boolean = false) {
        val sortedMedicines = if (isFiltering) medicines else medicines.sortedByDescending { it.id ?: 0 }
        binding.tvResultsCount.text = "${sortedMedicines.size} medicines found"
        binding.layoutMedicines.removeAllViews()

        if (sortedMedicines.isEmpty()) {
            return
        }

        sortedMedicines.forEach { medicine ->
            val itemBinding = ItemMedicineCardBinding.inflate(LayoutInflater.from(this), binding.layoutMedicines, false)
            itemBinding.tvMedicineName.text = medicine.name
            itemBinding.tvManufacturer.text = medicine.manufacturer ?: "Unknown"
            
            Glide.with(this)
                .load(RetrofitClient.getImageUrl(medicine.mainImage))
                .placeholder(R.drawable.pill_placeholder)
                .into(itemBinding.ivMedicine)

            itemBinding.root.setOnClickListener {
                val intent = Intent(this, MedicineDetailsActivity::class.java).apply {
                    putExtra("medicine_id", medicine.id)
                    putExtra("medicine_name", medicine.name)
                    putExtra("manufacturer", medicine.manufacturer)
                    putExtra("dosage", medicine.dosage)
                    putExtra("expiry_date", medicine.expiryDate)
                    putExtra("batch_number", medicine.batchNumber)
                    putExtra("category", medicine.category)
                    putExtra("main_image_uri", medicine.mainImage)
                    putExtra("front_image_uri", medicine.frontImage)
                    putExtra("back_image_uri", medicine.backImage)
                }
                startActivity(intent)
            }

            binding.layoutMedicines.addView(itemBinding.root)
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
            finish()
        }
        binding.navScan.setOnClickListener {
            val intent = Intent(this, ScanPillActivity::class.java)
            startActivity(intent)
        }
        binding.navIdentify.setOnClickListener {
            val intent = Intent(this, IdentifyPillActivity::class.java)
            startActivity(intent)
        }
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
