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
// Removed InputFilter and Spanned imports as they're no longer needed for this behavior

@Suppress("DEPRECATION")
class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupPasswordToggle()
    }

    private fun setupPasswordToggle() {
        var isPasswordVisible = false
        binding.ivTogglePassword.setOnClickListener {
            if (isPasswordVisible) {
                binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_off)
            } else {
                binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye)
            }
            binding.etPassword.setSelection(binding.etPassword.text.length)
            isPasswordVisible = !isPasswordVisible
        }

        var isConfirmPasswordVisible = false
        binding.ivToggleConfirmPassword.setOnClickListener {
            if (isConfirmPasswordVisible) {
                binding.etConfirmPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.ivToggleConfirmPassword.setImageResource(R.drawable.ic_eye_off)
            } else {
                binding.etConfirmPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.ivToggleConfirmPassword.setImageResource(R.drawable.ic_eye)
            }
            binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
            isConfirmPasswordVisible = !isConfirmPasswordVisible
        }
    }


    private fun setupListeners() {
        binding.btnSignup.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Confirm Password Check
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Double check name contains only letters and spaces
            if (!fullName.all { it.isLetter() || it == ' ' }) {
                Toast.makeText(this, "username should only contains letters and spaces", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Password Validation
            val missingCriteria = mutableListOf<String>()
            if (password.length < 6) missingCriteria.add("at least 6 characters long")
            if (!password.any { it.isUpperCase() }) missingCriteria.add("one uppercase letter")
            if (!password.any { it.isDigit() }) missingCriteria.add("one digit")
            if (!password.any { !it.isLetterOrDigit() }) missingCriteria.add("one special character")

            if (missingCriteria.isNotEmpty()) {
                val message = "Password must contain " + missingCriteria.joinToString(", ")
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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
