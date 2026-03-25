package com.simats.scanmypills

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.scanmypills.databinding.ActivityOtpVerificationBinding
import com.simats.scanmypills.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("DEPRECATION")
class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpVerificationBinding
    private var countDownTimer: CountDownTimer? = null
    private var email: String? = null
    private var isResendEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra("email")
        if (email == null) {
            Toast.makeText(this, "Email is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupOtpInputs()
        setupListeners()
        startTimer()
    }

    private fun setupOtpInputs() {
        val editTexts = arrayOf(
            binding.etOtp1, binding.etOtp2, binding.etOtp3,
            binding.etOtp4, binding.etOtp5, binding.etOtp6
        )

        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < editTexts.size - 1) {
                        editTexts[i + 1].requestFocus()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            editTexts[i].setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (editTexts[i].text.isEmpty() && i > 0) {
                        editTexts[i - 1].requestFocus()
                    }
                }
                false
            }
        }
    }

    private fun setupListeners() {
        binding.btnVerify.setOnClickListener {
            val otp = getOtpFromInputs()
            if (otp.length < 6) {
                Toast.makeText(this, "Please enter 6-digit code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyOtp(otp)
        }

        binding.tvTimer.setOnClickListener {
            if (isResendEnabled) {
                resendOtp()
            }
        }

        binding.tvChangeEmail.setOnClickListener {
            finish()
        }
    }

    private fun getOtpFromInputs(): String {
        return binding.etOtp1.text.toString() +
               binding.etOtp2.text.toString() +
               binding.etOtp3.text.toString() +
               binding.etOtp4.text.toString() +
               binding.etOtp5.text.toString() +
               binding.etOtp6.text.toString()
    }

    private fun verifyOtp(otp: String) {
        binding.btnVerify.isEnabled = false
        val request = VerifyOtpRequest(email!!, otp)

        RetrofitClient.instance.verifyOtp(request).enqueue(object : Callback<VerifyOtpResponse> {
            override fun onResponse(call: Call<VerifyOtpResponse>, response: Response<VerifyOtpResponse>) {
                binding.btnVerify.isEnabled = true
                if (response.isSuccessful) {
                    Toast.makeText(this@OtpVerificationActivity, response.body()?.message ?: "Verified", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@OtpVerificationActivity, CreateNewPasswordActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@OtpVerificationActivity, "Verification failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VerifyOtpResponse>, t: Throwable) {
                binding.btnVerify.isEnabled = true
                Toast.makeText(this@OtpVerificationActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun resendOtp() {
        isResendEnabled = false
        val request = ResendOtpRequest(email!!)

        RetrofitClient.instance.resendOtp(request).enqueue(object : Callback<ResendOtpResponse> {
            override fun onResponse(call: Call<ResendOtpResponse>, response: Response<ResendOtpResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@OtpVerificationActivity, response.body()?.message ?: "OTP Resent", Toast.LENGTH_SHORT).show()
                    startTimer()
                } else {
                    isResendEnabled = true
                    Toast.makeText(this@OtpVerificationActivity, "Failed to resend: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResendOtpResponse>, t: Throwable) {
                isResendEnabled = true
                Toast.makeText(this@OtpVerificationActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun startTimer() {
        isResendEnabled = false
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvTimer.text = String.format("Resend code: 00:%02ds", seconds)
                binding.tvTimer.setTextColor(resources.getColor(R.color.text_soft_grey, theme))
            }

            override fun onFinish() {
                binding.tvTimer.text = "Resend Code"
                binding.tvTimer.setTextColor(resources.getColor(R.color.btn_blue, theme))
                isResendEnabled = true
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
