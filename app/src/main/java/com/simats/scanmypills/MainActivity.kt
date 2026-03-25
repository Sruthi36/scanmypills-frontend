package com.simats.scanmypills

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.simats.scanmypills.databinding.ActivityMainBinding
import com.simats.scanmypills.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Toast
import android.view.View
import android.content.Context

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadImages()
        loadNavIcons()
        
        // Simple entrance animation for cards
        animateEntrance()
    }

    private fun animateEntrance() {
        binding.statsRow.alpha = 0f
        binding.statsRow.translationY = 50f
        binding.statsRow.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(200).start()

        binding.quickActionsRow.alpha = 0f
        binding.quickActionsRow.translationY = 50f
        binding.quickActionsRow.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(400).start()
    }

    override fun onResume() {
        super.onResume()
        fetchMedicines()
        fetchReminders()
    }

    private fun setupUI() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userName = prefs.getString("user_name", "user")
        binding.tvWelcome.text = "Hello, $userName!"

        binding.tvViewAll.setOnClickListener {
            startActivity(Intent(this, AllMedicinesActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.cardScan.setOnClickListener {
            startActivity(Intent(this, ScanPillActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.cardIdentify.setOnClickListener {
            startActivity(Intent(this, IdentifyPillActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.navScan.setOnClickListener {
            startActivity(Intent(this, ScanPillActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.navIdentify.setOnClickListener {
            startActivity(Intent(this, IdentifyPillActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.navReminders.setOnClickListener {
            startActivity(Intent(this, RemindersActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.navProfile.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun fetchMedicines() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val token = prefs.getString("auth_token", null) ?: return

        binding.progressBar.visibility = View.VISIBLE
        RetrofitClient.instance.getAllMedicines("Bearer $token")
            .enqueue(object : Callback<AllMedicinesResponse> {
                override fun onResponse(call: Call<AllMedicinesResponse>, response: Response<AllMedicinesResponse>) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.success == true) {
                        val medicines = response.body()?.medicines ?: emptyList()
                        binding.tvMedicineCount.text = (response.body()?.count ?: medicines.size).toString()
                        updateMedicinesUI(medicines)
                    } else {
                        Toast.makeText(this@MainActivity, "Server busy, medicines may load late.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AllMedicinesResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "Check internet connection...", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchReminders() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val token = prefs.getString("auth_token", null) ?: return

        RetrofitClient.instance.getAllRemindersGlobal("Bearer $token")
            .enqueue(object : Callback<GlobalRemindersResponse> {
                override fun onResponse(call: Call<GlobalRemindersResponse>, response: Response<GlobalRemindersResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        var totalCount = 0
                        response.body()?.reminders?.forEach { medGroup ->
                            totalCount += medGroup.items.size
                        }
                        binding.tvReminderCount.text = totalCount.toString()
                        
                        // Sync local cache for alarms
                        syncLocalCache(response.body()?.reminders ?: emptyList())
                    }
                }

                override fun onFailure(call: Call<GlobalRemindersResponse>, t: Throwable) {
                    // Fail silently for stats
                }
            })
    }

    private fun syncLocalCache(reminders: List<MedicineReminders>) {
        val rPrefs = getSharedPreferences("ScanMyPillsReminders", Context.MODE_PRIVATE)
        val editor = rPrefs.edit()
        editor.clear()
        
        val alarmScheduler = AlarmScheduler(this)
        reminders.forEach { medGroup ->
            medGroup.items.forEach { r ->
                if (r.isActive == 1) {
                    editor.putString(r.id.toString(), "${r.id}|${r.reminderTime}|${medGroup.medicineName}|${r.dosage}")
                    alarmScheduler.scheduleAlarm(r.id.toString(), r.reminderTime ?: "", medGroup.medicineName, r.dosage ?: "")
                }
            }
        }
        editor.apply()
    }

    private fun updateMedicinesUI(medicines: List<MedicineData>) {
        val sortedMedicines = medicines.sortedByDescending { it.id ?: 0 }

        if (sortedMedicines.isEmpty()) {
            binding.cardMed1.visibility = View.GONE
            binding.cardMed2.visibility = View.GONE
            return
        }

        if (sortedMedicines.isNotEmpty()) {
            val med1 = sortedMedicines[0]
            binding.cardMed1.visibility = View.VISIBLE
            binding.tvMedName1.text = med1.name
            binding.tvMedManufacturer1.text = med1.manufacturer ?: "Unknown"
            binding.tvMedDosage1.text = med1.dosage ?: "No dosage set"
            
            Glide.with(this)
                .load(RetrofitClient.getImageUrl(med1.mainImage))
                .placeholder(R.drawable.pill_placeholder)
                .into(binding.ivMed1)

            binding.cardMed1.setOnClickListener {
                navigateToDetails(med1)
            }
        }

        if (sortedMedicines.size >= 2) {
            val med2 = sortedMedicines[1]
            binding.cardMed2.visibility = View.VISIBLE
            binding.tvMedName2.text = med2.name
            binding.tvMedManufacturer2.text = med2.manufacturer ?: "Unknown"
            binding.tvMedDosage2.text = med2.dosage ?: "No dosage set"

            Glide.with(this)
                .load(RetrofitClient.getImageUrl(med2.mainImage))
                .placeholder(R.drawable.pill_placeholder)
                .into(binding.ivMed2)

            binding.cardMed2.setOnClickListener {
                navigateToDetails(med2)
            }
        } else {
            binding.cardMed2.visibility = View.GONE
        }
    }

    private fun navigateToDetails(med: MedicineData) {
        val intent = Intent(this, MedicineDetailsActivity::class.java).apply {
            putExtra("medicine_id", med.id)
            putExtra("medicine_name", med.name)
            putExtra("manufacturer", med.manufacturer)
            putExtra("dosage", med.dosage)
            putExtra("expiry_date", med.expiryDate)
            putExtra("batch_number", med.batchNumber)
            putExtra("category", med.category)
            putExtra("main_image_uri", med.mainImage)
            putExtra("front_image_uri", med.frontImage)
            putExtra("back_image_uri", med.backImage)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun loadImages() {
        val icon1Url = "https://image2url.com/r2/default/images/1772005965600-46d478a1-ea60-456d-8e2e-9cb1c98eabfb.png"
        val icon2Url = "https://image2url.com/r2/default/images/1772006103545-369d46f8-968c-4ffb-a0d4-73c5b634ddd8.png"
        val icon3Url = "https://image2url.com/r2/default/images/1772006152755-cda8dae5-683e-44ed-90fd-d9ea761b492e.png"
        val icon4Url = "https://image2url.com/r2/default/images/1772006220137-f596d911-1a24-4ef8-a047-f9410e52d95d.png"

        Glide.with(this).load(icon1Url).into(binding.ivStatMedicines)
        Glide.with(this).load(icon2Url).into(binding.ivStatReminders)
        Glide.with(this).load(icon3Url).into(binding.ivActionScan)
        Glide.with(this).load(icon4Url).into(binding.ivActionIdentify)
    }

        private fun loadNavIcons() {
        binding.ivNavHome.setImageResource(R.drawable.ic_home)
        binding.ivNavScan.setImageResource(R.drawable.ic_barcode_scanner)
        binding.ivNavIdentify.setImageResource(R.drawable.ic_camera)
        binding.ivNavReminders.setImageResource(R.drawable.ic_bell)
        binding.ivNavProfile.setImageResource(R.drawable.ic_settings)
        
        requestRequiredPermissions()
    }

    private fun requestRequiredPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permissions = arrayOf(android.Manifest.permission.POST_NOTIFICATIONS)
            requestPermissions(permissions, 101)
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                android.widget.Toast.makeText(this, "Please enable Exact Alarm permission", android.widget.Toast.LENGTH_LONG).show()
            }
        }
        
        // Overlay permission as a fallback for full-screen reliability
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!android.provider.Settings.canDrawOverlays(this)) {
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName")
                )
                startActivity(intent)
                android.widget.Toast.makeText(this, "Please allow 'Display over other apps' for alarms to show full screen", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
}
