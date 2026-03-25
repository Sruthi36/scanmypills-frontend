package com.simats.scanmypills

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.bumptech.glide.Glide
import com.simats.scanmypills.databinding.*
import com.simats.scanmypills.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("DEPRECATION")
class RemindersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemindersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        populateSchedule()
        populateAllReminders()
        loadNavIcons()
        setupNavbar()
    }

    private fun formatTo12Hour(timeStr: String): String {
        if (timeStr.isEmpty()) return ""
        return try {
            val inputFormats = arrayOf("HH:mm", "HH:mm:ss", "hh:mm a", "H:mm")
            var date: java.util.Date? = null
            for (format in inputFormats) {
                try {
                    val sdf = java.text.SimpleDateFormat(format, java.util.Locale.US)
                    sdf.isLenient = false
                    date = sdf.parse(timeStr)
                    if (date != null) break
                } catch (e: Exception) {}
            }

            if (date != null) {
                val outputFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
                outputFormat.format(date).uppercase()
            } else {
                timeStr
            }
        } catch (e: Exception) {
            timeStr
        }
    }

    private fun isTimePast(timeStr: String): Boolean {
        if (timeStr.isEmpty()) return false
        try {
            val formats = arrayOf("HH:mm", "HH:mm:ss", "hh:mm a", "H:mm")
            var date: java.util.Date? = null
            for (format in formats) {
                try {
                    val sdf = java.text.SimpleDateFormat(format, java.util.Locale.US)
                    date = sdf.parse(timeStr)
                    if (date != null) break
                } catch (e: Exception) {}
            }
            
            if (date == null) return false
            
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply { time = date }
            
            target.set(java.util.Calendar.YEAR, now.get(java.util.Calendar.YEAR))
            target.set(java.util.Calendar.MONTH, now.get(java.util.Calendar.MONTH))
            target.set(java.util.Calendar.DAY_OF_YEAR, now.get(java.util.Calendar.DAY_OF_YEAR))
            
            // 15-second grace period only to avoid flashing "Past" during the actual minute
            return target.timeInMillis < (now.timeInMillis - 15000)
        } catch (e: Exception) {
            return false
        }
    }

    private fun populateSchedule() {
        val prefs = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null) ?: return

        binding.scheduleList.removeAllViews()

        RetrofitClient.instance.getSchedule("Bearer $token")
            .enqueue(object : retrofit2.Callback<ScheduleResponse> {
                override fun onResponse(call: retrofit2.Call<ScheduleResponse>, response: retrofit2.Response<ScheduleResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val scheduleItems = response.body()?.schedule ?: emptyList()
                        
                        // Group by time
                        val groupedByTime = scheduleItems.groupBy { it.time }
                        
                        // Sort times
                        val sortedTimes = groupedByTime.keys.sorted()
                        
                        sortedTimes.forEach { timeStr ->
                            val itemsAtTime = groupedByTime[timeStr] ?: return@forEach
                            val itemBinding = ItemScheduleBinding.inflate(layoutInflater, binding.scheduleList, false)
                            
                            itemBinding.tvTime.text = formatTo12Hour(timeStr)
                            
                            // Combine names and dosages into "Name - Dosage" format
                            val combinedDetails = itemsAtTime.joinToString("\n") { 
                                val dosageDisplay = if (it.dosage.all { c -> c.isDigit() }) {
                                    if (it.dosage == "1") "1 Tablet" else "${it.dosage} Tablets"
                                } else it.dosage
                                "${it.medicineName} - $dosageDisplay"
                            }
                            
                            itemBinding.tvName.text = combinedDetails
                            itemBinding.tvDosage.visibility = View.GONE
                            
                            // Status check (if any is past, mark group as past - or be more granular?)
                            // For simplicity, if the time is past, the whole card is past
                            val isPast = isTimePast(timeStr)
                            if (isPast) {
                                itemBinding.cardStatus.visibility = View.VISIBLE
                                itemBinding.rootLayout.alpha = 0.5f
                                itemBinding.root.setCardBackgroundColor(android.graphics.Color.WHITE)
                                itemBinding.root.strokeColor = android.graphics.Color.parseColor("#E5E7EB")
                                itemBinding.root.cardElevation = 0f
                            } else {
                                itemBinding.cardStatus.visibility = View.GONE
                                itemBinding.rootLayout.alpha = 1.0f
                                itemBinding.root.strokeColor = getColor(R.color.btn_blue)
                                itemBinding.root.setCardBackgroundColor(android.graphics.Color.parseColor("#F0F8FF"))
                                itemBinding.tvTime.setTextColor(getColor(R.color.btn_blue))
                                itemBinding.root.cardElevation = 4f
                            }
                            binding.scheduleList.addView(itemBinding.root)
                        }
                    }
                }

                override fun onFailure(call: retrofit2.Call<ScheduleResponse>, t: Throwable) {
                    android.widget.Toast.makeText(this@RemindersActivity, "Schedule error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun syncAllLocalReminders(reminders: List<MedicineReminders>) {
        val rPrefs = getSharedPreferences("ScanMyPillsReminders", android.content.Context.MODE_PRIVATE)
        val editor = rPrefs.edit()
        editor.clear() // Clear all to sync fresh global list
        
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

    private fun populateAllReminders() {
        val prefs = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null) ?: return

        RetrofitClient.instance.getAllRemindersGlobal("Bearer $token")
            .enqueue(object : retrofit2.Callback<GlobalRemindersResponse> {
                override fun onResponse(call: retrofit2.Call<GlobalRemindersResponse>, response: retrofit2.Response<GlobalRemindersResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val reminders = response.body()?.reminders ?: emptyList()
                        syncAllLocalReminders(reminders)
                        
                        binding.allRemindersContainer.removeAllViews()
                        reminders.forEach { medGroup ->
                            val groupBinding = ItemMedicineReminderBinding.inflate(layoutInflater, binding.allRemindersContainer, false)
                            groupBinding.tvMedicineName.text = medGroup.medicineName
                            groupBinding.tvReminderCount.text = "${medGroup.items.size} reminders"
                            
                            Glide.with(this@RemindersActivity)
                                .load(RetrofitClient.getImageUrl(medGroup.mainImage))
                                .placeholder(R.drawable.pill_placeholder)
                                .into(groupBinding.ivMedicine)
                            
                            val alarmScheduler = AlarmScheduler(this@RemindersActivity)

                            medGroup.items.forEach { r ->
                                val rowBinding = ItemReminderRowBinding.inflate(layoutInflater, groupBinding.remindersList, false)
                                rowBinding.tvTime.text = formatTo12Hour(r.reminderTime ?: "")
                                rowBinding.tvInfo.text = r.dosage
                                rowBinding.ivToggle.setImageResource(if (r.isActive == 1) R.drawable.ic_toggle_on else R.drawable.ic_toggle_off)
                                
                                rowBinding.ivToggle.setOnClickListener {
                                    val newStatus = r.isActive != 1
                                    RetrofitClient.instance.toggleReminder("Bearer $token", r.id)
                                        .enqueue(object : retrofit2.Callback<ToggleReminderResponse> {
                                            override fun onResponse(c: retrofit2.Call<ToggleReminderResponse>, res: retrofit2.Response<ToggleReminderResponse>) {
                                                if (res.isSuccessful) {
                                                    if (!newStatus) {
                                                        // Explicitly cancel if turned off
                                                        alarmScheduler.cancelAlarm(r.id.toString())
                                                    }
                                                    populateAllReminders()
                                                    populateSchedule()
                                                }
                                            }
                                            override fun onFailure(c: retrofit2.Call<ToggleReminderResponse>, t: Throwable) {}
                                        })
                                }

                                rowBinding.ivDelete.setOnClickListener {
                                    RetrofitClient.instance.deleteReminder("Bearer $token", r.id)
                                        .enqueue(object : retrofit2.Callback<DeleteReminderResponse> {
                                            override fun onResponse(c: retrofit2.Call<DeleteReminderResponse>, res: retrofit2.Response<DeleteReminderResponse>) {
                                                if (res.isSuccessful) {
                                                    // Explicitly cancel OS alarm
                                                    alarmScheduler.cancelAlarm(r.id.toString())
                                                    populateAllReminders()
                                                    populateSchedule()
                                                }
                                            }
                                            override fun onFailure(c: retrofit2.Call<DeleteReminderResponse>, t: Throwable) {}
                                        })
                                }
                                groupBinding.remindersList.addView(rowBinding.root)
                            }
                            binding.allRemindersContainer.addView(groupBinding.root)
                        }
                    }
                }

                override fun onFailure(call: retrofit2.Call<GlobalRemindersResponse>, t: Throwable) {
                    android.widget.Toast.makeText(this@RemindersActivity, "Reminders error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            })
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

        binding.navIdentify.setOnClickListener {
            val intent = Intent(this, IdentifyPillActivity::class.java)
            startActivity(intent)
        }

        // Reminders is active
        binding.navReminders.setOnClickListener { }

        binding.navProfile.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
