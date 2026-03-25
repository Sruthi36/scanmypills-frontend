package com.simats.scanmypills

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.simats.scanmypills.databinding.ActivitySignupBinding
import com.simats.scanmypills.network.RegisterRequest
import com.simats.scanmypills.network.RegisterResponse
import com.simats.scanmypills.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import android.util.Patterns

@Suppress("DEPRECATION")
class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }


    private fun setupListeners() {
        binding.btnSignup.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(fullName, email, password)
        }

        binding.tvSignin.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        binding.btnSignup.isEnabled = false
        val request = RegisterRequest(name, email, password)

        RetrofitClient.instance.register(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                binding.btnSignup.isEnabled = true
                if (response.isSuccessful) {
                    val body = response.body()
                    Toast.makeText(this@SignupActivity, body?.message ?: "Success", Toast.LENGTH_SHORT).show()
                    
                    // Save to prefs
                    val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    prefs.edit()
                        .putString("user_name", name)
                        .putString("user_email", email)
                        .apply()

                    val intent = Intent(this@SignupActivity, TermsActivity::class.java)
                    intent.putExtra("from_signup", true)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                } else {
                    Toast.makeText(this@SignupActivity, "Registration failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                binding.btnSignup.isEnabled = true
                Toast.makeText(this@SignupActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
