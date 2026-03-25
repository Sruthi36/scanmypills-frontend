package com.simats.scanmypills

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.simats.scanmypills.databinding.ActivityChangePasswordBinding
import com.simats.scanmypills.databinding.DialogPasswordUpdatedBinding

import android.content.Intent
import com.simats.scanmypills.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("DEPRECATION")
class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    
    private var isCurrentPasswordVisible = false
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupListeners() {
        // Password Visibility Toggles
        binding.ivToggleCurrent.setOnClickListener {
            isCurrentPasswordVisible = !isCurrentPasswordVisible
            togglePasswordVisibility(binding.etCurrentPassword, binding.ivToggleCurrent, isCurrentPasswordVisible)
        }

        binding.ivToggleNew.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            togglePasswordVisibility(binding.etNewPassword, binding.ivToggleNew, isNewPasswordVisible)
        }

        binding.ivToggleConfirm.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(binding.etConfirmPassword, binding.ivToggleConfirm, isConfirmPasswordVisible)
        }

        binding.btnUpdatePassword.setOnClickListener {
            if (validatePasswords()) {
                callChangePasswordApi()
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun callChangePasswordApi() {
        val current = binding.etCurrentPassword.text.toString()
        val new = binding.etNewPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()

        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)
        
        if (token == null) {
            Toast.makeText(this, "Session expired, please login again", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnUpdatePassword.isEnabled = false
        val request = ChangePassRequest(current, new, confirm)
        val bearerToken = "Bearer $token"

        RetrofitClient.instance.changePassword(bearerToken, request)
            .enqueue(object : Callback<GeneralResponse> {
                override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                    binding.btnUpdatePassword.isEnabled = true
                    if (response.isSuccessful) {
                        showSuccessDialog()
                    } else {
                        val error = response.body()?.error ?: "Update failed: ${response.code()}"
                        Toast.makeText(this@ChangePasswordActivity, error, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                    binding.btnUpdatePassword.isEnabled = true
                    Toast.makeText(this@ChangePasswordActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun togglePasswordVisibility(editText: EditText, imageView: ImageView, isVisible: Boolean) {
        if (isVisible) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            imageView.setImageResource(R.drawable.ic_eye_off)
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            imageView.setImageResource(R.drawable.ic_eye)
        }
        editText.setSelection(editText.text.length)
    }

    private fun validatePasswords(): Boolean {
        val current = binding.etCurrentPassword.text.toString()
        val new = binding.etNewPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()

        if (current.isEmpty()) {
            Toast.makeText(this, "Please enter your current password", Toast.LENGTH_SHORT).show()
            return false
        }
        if (new.length < 8) {
            Toast.makeText(this, "New password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        if (new != confirm) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun showSuccessDialog() {
        val dialogBinding = DialogPasswordUpdatedBinding.inflate(LayoutInflater.from(this))
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
