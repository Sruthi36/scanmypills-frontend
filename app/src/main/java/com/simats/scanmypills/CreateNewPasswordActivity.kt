package com.simats.scanmypills

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.simats.scanmypills.databinding.ActivityCreateNewPasswordBinding
import com.simats.scanmypills.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("DEPRECATION")
class CreateNewPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateNewPasswordBinding
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNewPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra("email")
        if (email == null) {
            Toast.makeText(this, "Email is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupListeners()
    }


    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        binding.btnResetPassword.setOnClickListener {
            val newPassword = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please enter and confirm your password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            resetPassword(newPassword)
        }
    }

    private fun resetPassword(password: String) {
        binding.btnResetPassword.isEnabled = false
        val request = ResetPassRequest(email!!, password)

        RetrofitClient.instance.resetPassword(request).enqueue(object : Callback<ResetPassResponse> {
            override fun onResponse(call: Call<ResetPassResponse>, response: Response<ResetPassResponse>) {
                binding.btnResetPassword.isEnabled = true
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateNewPasswordActivity, response.body()?.message ?: "Password reset successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@CreateNewPasswordActivity, PasswordResetSuccessActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@CreateNewPasswordActivity, "Reset failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResetPassResponse>, t: Throwable) {
                binding.btnResetPassword.isEnabled = true
                Toast.makeText(this@CreateNewPasswordActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

}
