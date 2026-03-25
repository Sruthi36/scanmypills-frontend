package com.simats.scanmypills

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Restore alarms on device boot
            val prefs = context.getSharedPreferences("ScanMyPillsReminders", Context.MODE_PRIVATE)
            val allEntries = prefs.all
            
            val alarmScheduler = AlarmScheduler(context)
            
            for ((key, value) in allEntries) {
                if (value is String) {
                    // Assuming format: id|timeString|medicineName|dosage
                    val parts = value.split("|")
                    if (parts.size >= 4) {
                        val id = parts[0]
                        val timeStr = parts[1]
                        val name = parts[2]
                        val dosage = parts[3]
                        
                        alarmScheduler.scheduleAlarm(id, timeStr, name, dosage)
                    }
                }
            }
        }
    }
}
