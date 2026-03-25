package com.simats.scanmypills

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.simats.scanmypills.databinding.ActivityAlarmRingBinding

class AlarmRingActivity : AppCompatActivity() {
 
    private lateinit var binding: ActivityAlarmRingBinding
    private val currentReminderIds = mutableListOf<String>()

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        displayMedicineInfo()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }
        
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        binding = ActivityAlarmRingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        displayMedicineInfo()

        binding.btnTake.setOnClickListener { dismissAllInGroup() }
        binding.btnSkip.setOnClickListener { dismissAllInGroup() }
    }

    private fun displayMedicineInfo() {
        val scheduler = AlarmScheduler(this)
        
        val triggerMedicineName = intent.getStringExtra("medicine_name") ?: "Medicine"
        val triggerDosage = intent.getStringExtra("dosage") ?: ""
        val triggerReminderId = intent.getStringExtra("reminder_id") ?: ""
        val triggerTime = intent.getStringExtra("original_time") ?: ""
        val normalizedTriggerTime = scheduler.normalizeTime(triggerTime)

        android.util.Log.d("AlarmRingActivity", "Displaying info for time: $normalizedTriggerTime (raw: $triggerTime)")

        binding.medicineList.removeAllViews()
        currentReminderIds.clear()

        val rPrefs = getSharedPreferences("ScanMyPillsReminders", Context.MODE_PRIVATE)
        val allReminders = rPrefs.all
        
        val medicinesToDisplay = mutableListOf<Triple<String, String, String>>()

        if (normalizedTriggerTime.isNotEmpty()) {
            allReminders.forEach { (id, value) ->
                val parts = (value as? String)?.split("|") ?: return@forEach
                if (parts.size >= 4) {
                    val entryTime = scheduler.normalizeTime(parts[1])
                    if (entryTime == normalizedTriggerTime) {
                        medicinesToDisplay.add(Triple(parts[2], parts[3], id))
                        if (!currentReminderIds.contains(id)) currentReminderIds.add(id)
                    }
                }
            }
        }

        // Add trigger medicine if not already in list
        if (!currentReminderIds.contains(triggerReminderId)) {
            medicinesToDisplay.add(Triple(triggerMedicineName, triggerDosage, triggerReminderId))
            if (triggerReminderId.isNotEmpty()) currentReminderIds.add(triggerReminderId)
        }

        // Remove duplicates and display
        medicinesToDisplay.distinctBy { it.first.trim().lowercase() + it.second.trim().lowercase() }.forEach { triple ->
            addMedicineRow(triple.first, triple.second)
        }
    }

    private fun addMedicineRow(name: String, dosage: String) {
        val nameView = TextView(this).apply {
            text = name
            setTextColor(getColor(R.color.primary))
            textSize = 32f
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
        }

        val dosageView = TextView(this).apply {
            text = formatDosage(dosage)
            setTextColor(getColor(R.color.text_soft_grey))
            textSize = 16f
            setBackgroundResource(R.drawable.chip_bg)
            setPadding(20.dpToPx(), 8.dpToPx(), 20.dpToPx(), 8.dpToPx())
            gravity = android.view.Gravity.CENTER
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { 
                setMargins(0, 8.dpToPx(), 0, 24.dpToPx())
                gravity = android.view.Gravity.CENTER
            }
            layoutParams = params
        }

        binding.medicineList.addView(nameView)
        binding.medicineList.addView(dosageView)
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun formatDosage(dosage: String): String {
        if (dosage.isEmpty()) return ""
        return if (dosage.all { it.isDigit() }) {
            if (dosage == "1") "1 Tablet" else "$dosage Tablets"
        } else {
            dosage
        }
    }

    private fun dismissAllInGroup() {
        stopService(Intent(this, AlarmService::class.java))
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        currentReminderIds.forEach { id ->
            notificationManager.cancel(id.hashCode())
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, AlarmService::class.java))
    }
}
