package com.simats.scanmypills

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.simats.scanmypills.databinding.ActivityNotificationsBinding
import com.simats.scanmypills.databinding.DialogPreferencesSavedBinding

@Suppress("DEPRECATION")
class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private val PREFS_NAME = "NotificationPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadPreferences()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadPreferences() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        binding.switchAll.isChecked = prefs.getBoolean("all_notifications", true)
        binding.switchSystem.isChecked = prefs.getBoolean("system_announcements", true)
        binding.switchEmail.isChecked = prefs.getBoolean("email_notifications", true)
        binding.switchInApp.isChecked = prefs.getBoolean("in_app_notifications", false)
        binding.switchSound.isChecked = prefs.getBoolean("sound_alerts", true)
        
        updateSubToggles(binding.switchAll.isChecked)
    }

    private fun setupListeners() {
        binding.switchAll.setOnCheckedChangeListener { _, isChecked ->
            updateSubToggles(isChecked)
        }

        binding.btnSavePreferences.setOnClickListener {
            savePreferences()
            showSuccessDialog()
        }

        binding.btnResetChanges.setOnClickListener {
            loadPreferences()
        }
    }

    private fun updateSubToggles(enabled: Boolean) {
        binding.switchSystem.isEnabled = enabled
        binding.switchEmail.isEnabled = enabled
        binding.switchInApp.isEnabled = enabled
        binding.switchSound.isEnabled = enabled
        
        if (!enabled) {
            binding.switchSystem.isChecked = false
            binding.switchEmail.isChecked = false
            binding.switchInApp.isChecked = false
            binding.switchSound.isChecked = false
        }
    }

    private fun savePreferences() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putBoolean("all_notifications", binding.switchAll.isChecked)
            .putBoolean("system_announcements", binding.switchSystem.isChecked)
            .putBoolean("email_notifications", binding.switchEmail.isChecked)
            .putBoolean("in_app_notifications", binding.switchInApp.isChecked)
            .putBoolean("sound_alerts", binding.switchSound.isChecked)
            .apply()
    }

    private fun showSuccessDialog() {
        val dialogBinding = DialogPreferencesSavedBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.btnDone.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }
}
