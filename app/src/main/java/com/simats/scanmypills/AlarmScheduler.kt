package com.simats.scanmypills

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.concurrent.thread

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Normalizes time to a standard "HH:mm" format for grouping.
     * Handles various input formats and is whitespace/case insensitive.
     */
    fun normalizeTime(timeString: String?): String {
        if (timeString.isNullOrEmpty()) return ""
        
        // Clean the input: trim, remove extra spaces, uppercase
        val cleanTime = timeString.trim().replace("\\s+".toRegex(), " ").uppercase()
        
        val formats = arrayOf(
            "HH:mm", 
            "hh:mm a", 
            "HH:mm:ss", 
            "H:mm",
            "h:mm a"
        )
        
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                sdf.isLenient = true
                val date = sdf.parse(cleanTime)
                if (date != null) {
                    return SimpleDateFormat("HH:mm", Locale.US).format(date)
                }
            } catch (e: Exception) {}
        }
        
        // If all parsing fails, try manual stripping of AM/PM if they are attached without space
        if (cleanTime.endsWith("AM") || cleanTime.endsWith("PM")) {
            val digitsOnly = cleanTime.filter { it.isDigit() || it == ':' }
            if (digitsOnly.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("H:mm", Locale.US)
                    val date = sdf.parse(digitsOnly)
                    if (date != null) {
                        var hour = Calendar.getInstance().apply { time = date }.get(Calendar.HOUR_OF_DAY)
                        if (cleanTime.contains("PM") && hour < 12) hour += 12
                        if (cleanTime.contains("AM") && hour == 12) hour = 0
                        return String.format(Locale.US, "%02d:%02d", hour, Calendar.getInstance().apply { time = date }.get(Calendar.MINUTE))
                    }
                } catch (e: Exception) {}
            }
        }

        return cleanTime
    }

    fun scheduleAlarm(reminderId: String, timeString: String, medicineName: String, dosage: String) {
        val normalized = normalizeTime(timeString)
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.simats.scanmypills.ALARM_TRIGGER"
            data = Uri.parse("alarm://$reminderId")
            putExtra("reminder_id", reminderId)
            putExtra("medicine_name", medicineName)
            putExtra("dosage", dosage)
            putExtra("original_time", normalized)
        }

        val requestCode = reminderId.hashCode() + 200000
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
        )

        val calendar = Calendar.getInstance()
        val formats = arrayOf("HH:mm", "hh:mm a", "HH:mm:ss", "H:mm")
        var date: java.util.Date? = null
        
        val cleanTime = timeString.trim().replace("\\s+".toRegex(), " ").uppercase()
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                date = sdf.parse(cleanTime)
                if (date != null) break
            } catch (e: Exception) {}
        }

        if (date != null) {
            val parsedCalendar = Calendar.getInstance().apply { time = date }
            calendar.set(Calendar.HOUR_OF_DAY, parsedCalendar.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, parsedCalendar.get(Calendar.MINUTE))
        } else {
            return
        }

        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        try {
            val showIntent = Intent(context, RemindersActivity::class.java)
            val showPendingIntent = PendingIntent.getActivity(
                context, 0, showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val alarmInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, showPendingIntent)
            alarmManager.setAlarmClock(alarmInfo, pendingIntent)
        } catch (e: Exception) {
            android.util.Log.e("AlarmScheduler", "Scheduling failed: ${e.message}")
        }
    }

    fun cancelAlarm(reminderId: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.simats.scanmypills.ALARM_TRIGGER"
            data = Uri.parse("alarm://$reminderId")
        }
        val requestCode = reminderId.hashCode() + 200000
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Reschedules all alarms in a background thread to prevent UI jank.
     */
    fun rescheduleAllAlarms() {
        thread {
            try {
                val prefs = context.getSharedPreferences("ScanMyPillsReminders", Context.MODE_PRIVATE)
                val allEntries = prefs.all
                android.util.Log.d("AlarmScheduler", "Background rescheduling ${allEntries.size} alarms")
                
                for ((_, value) in allEntries) {
                    if (value is String) {
                        val parts = value.split("|")
                        if (parts.size >= 4) {
                            scheduleAlarm(parts[0], parts[1], parts[2], parts[3])
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AlarmScheduler", "Bg reschedule error: ${e.message}")
            }
        }
    }
}
