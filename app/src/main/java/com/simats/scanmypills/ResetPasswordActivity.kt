package com.simats.scanmypills

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.scanmypills.databinding.ActivityResetPasswordBinding
import com.simats.scanmypills.network.ForgotPassRequest
import com.simats.scanmypills.network.ForgotPassResponse
import com.simats.scanmypills.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("DEPRECATION")
class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }


    private fun setupListeners() {
        binding.tvBackToLogin.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binding.btnSendCode.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendOtp(email)
        }
    }

    private fun sendOtp(email: String) {
        binding.btnSendCode.isEnabled = false
        val request = ForgotPassRequest(email)

        RetrofitClient.instance.forgotPassword(request).enqueue(object : Callback<ForgotPassResponse> {
            override fun onResponse(call: Call<ForgotPassResponse>, response: Response<ForgotPassResponse>) {
                binding.btnSendCode.isEnabled = true
                if (response.isSuccessful) {
                    Toast.makeText(this@ResetPasswordActivity, response.body()?.message ?: "OTP sent", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ResetPasswordActivity, OtpVerificationActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                } else {
                    val code = response.code()
                    if (code == 429) {
                        val body = response.body() ?: response.errorBody()?.string()?.let { 
                            com.google.gson.Gson().fromJson(it, ForgotPassResponse::class.java)
                        }
                        val remaining = body?.secondsRemaining ?: 60
                        Toast.makeText(this@ResetPasswordActivity, "Too many requests. Wait $remaining seconds", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@ResetPasswordActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ForgotPassResponse>, t: Throwable) {
                binding.btnSendCode.isEnabled = true
                Toast.makeText(this@ResetPasswordActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
