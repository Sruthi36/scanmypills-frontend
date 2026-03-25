package com.simats.scanmypills

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.simats.scanmypills.databinding.ActivitySettingsBinding

import android.widget.Toast
import com.simats.scanmypills.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("DEPRECATION")
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        loadNavIcons()
        setupNavbar()
        loadUserData()
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val name = prefs.getString("user_name", "Profile Information")
        val email = prefs.getString("user_email", "user@example.com")
        binding.tvProfileName.text = name
        binding.tvProfileEmail.text = email
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupClickListeners() {
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        binding.btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        binding.btnNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        binding.btnPrivacyPolicy.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        binding.btnDataStorage.setOnClickListener {
            startActivity(Intent(this, DataStorageActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
        binding.btnSupport.setOnClickListener {
            startActivity(Intent(this, HelpSupportActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        binding.btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun showLogoutDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(this).inflate(R.layout.layout_custom_dialog, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog.window?.attributes = layoutParams

        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnConfirm)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)

        btnConfirm.setOnClickListener {
            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            dialog.dismiss()
            finish()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteAccountDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(this).inflate(R.layout.layout_custom_dialog, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog.window?.attributes = layoutParams

        val tvTitle = view.findViewById<TextView>(R.id.dialogTitle)
        val tvMessage = view.findViewById<TextView>(R.id.dialogMessage)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnConfirm)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)

        tvTitle.text = "Delete Account"
        tvMessage.text = "Are you sure you want to permanently delete your account? This action cannot be undone."
        btnConfirm.text = "Delete"
        btnConfirm.setBackgroundColor(Color.parseColor("#EF4444")) // Red color

        btnConfirm.setOnClickListener {
            // In a real app, you'd call a delete API here
            Toast.makeText(this, "Account deletion requested", Toast.LENGTH_SHORT).show()
            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            dialog.dismiss()
            finish()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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
            finish()
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
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
