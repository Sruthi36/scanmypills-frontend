package com.simats.scanmypills

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("AlarmReceiver", "Alarm trigger action: ${intent.action}, data: ${intent.data}")
        
        val reminderId = intent.getStringExtra("reminder_id") ?: return
        val medicineName = intent.getStringExtra("medicine_name") ?: "Medicine"
        val dosage = intent.getStringExtra("dosage") ?: ""
        val originalTime = intent.getStringExtra("original_time") ?: ""

        // Show notification with full-screen intent
        showNotification(context, reminderId, medicineName, dosage, originalTime)

        // Reschedule for next day
        AlarmScheduler(context).scheduleAlarm(reminderId, originalTime, medicineName, dosage)
        
        // Start alarm sound/vibration service
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("medicine_name", medicineName)
            putExtra("dosage", dosage)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun showNotification(context: Context, reminderId: String, medicineName: String, dosage: String, originalTime: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "scanmypills_alarms"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent medicine schedule alarms"
                enableVibration(true)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(context, AlarmRingActivity::class.java).apply {
            action = "com.simats.scanmypills.ALARM_RING"
            data = Uri.parse("alarm-ring://$reminderId")
            putExtra("reminder_id", reminderId)
            putExtra("medicine_name", medicineName)
            putExtra("dosage", dosage)
            putExtra("original_time", originalTime)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION or 
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode() + 600000,
            fullScreenIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_pill)
            .setContentTitle("Take your $medicineName")
            .setContentText("Dosage: $dosage")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setSilent(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

        notificationManager.notify(reminderId.hashCode(), builder.build())
        
        // Direct launch if possible (Android < 10 or with overlay permission)
        try {
            context.startActivity(fullScreenIntent)
        } catch (e: Exception) {
            android.util.Log.e("AlarmReceiver", "Background start blocked, relying on fullScreenIntent: ${e.message}")
        }
    }
}
