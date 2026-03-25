package com.simats.scanmypills

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.bumptech.glide.Glide
import com.simats.scanmypills.databinding.ActivityProfileBinding
import com.simats.scanmypills.databinding.DialogSuccessPopupBinding
import com.simats.scanmypills.network.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                selectedImageUri = uri
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(binding.ivProfilePhoto)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadUserData()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)
        
        if (userId != -1) {
            fetchUserProfile(userId)
        } else {
            // Fallback to locally stored info if id is missing
            val name = prefs.getString("user_name", "")
            val email = prefs.getString("user_email", "")
            val phone = prefs.getString("user_phone", "")

            binding.etFullName.setText(name)
            binding.etEmail.setText(email)
            binding.etPhone.setText(phone)
        }
    }

    private fun fetchUserProfile(userId: Int) {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val token = prefs.getString("auth_token", null) ?: return
        val bearerToken = "Bearer $token"

        RetrofitClient.instance.getUserProfile(bearerToken, userId)
            .enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        user?.let {
                            binding.etFullName.setText(it.name)
                            binding.etEmail.setText(it.email)
                            binding.etPhone.setText(it.phone ?: "")
                            
                            if (!it.profilePhoto.isNullOrEmpty()) {
                                binding.ivProfilePhoto.setPadding(0, 0, 0, 0)
                                binding.ivProfilePhoto.colorFilter = null
                                Glide.with(this@ProfileActivity)
                                    .load(RetrofitClient.getImageUrl(it.profilePhoto))
                                    .placeholder(R.drawable.ic_user)
                                    .error(R.drawable.ic_user)
                                    .into(binding.ivProfilePhoto)
                            }
                        }
                    } else if (response.code() == 401) {
                        Toast.makeText(this@ProfileActivity, "Session expired", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Error loading profile", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupListeners() {
        binding.btnUploadPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        binding.btnSaveChanges.setOnClickListener {
            saveUserData()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveUserData() {
        val name = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (phone.isNotEmpty() && phone.length < 10) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)
        val token = prefs.getString("auth_token", null)
        
        if (userId == -1 || token == null) {
            Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSaveChanges.isEnabled = false

        val nameBody = RequestBody.create(MediaType.parse("text/plain"), name)
        val phoneBody = RequestBody.create(MediaType.parse("text/plain"), phone)
        
        var photoPart: MultipartBody.Part? = null
        selectedImageUri?.let { uri ->
            val file = getFileFromUri(uri)
            file?.let {
                val requestFile = RequestBody.create(MediaType.parse("image/*"), it)
                photoPart = MultipartBody.Part.createFormData("profile_photo", it.name, requestFile)
            }
        }

        val bearerToken = "Bearer $token"
        RetrofitClient.instance.updateUserProfile(bearerToken, userId, nameBody, phoneBody, photoPart)
            .enqueue(object : Callback<GeneralResponse> {
                override fun onResponse(call: Call<GeneralResponse>, response: Response<GeneralResponse>) {
                    binding.btnSaveChanges.isEnabled = true
                    if (response.isSuccessful) {
                        prefs.edit {
                            putString("user_name", name)
                            putString("user_phone", phone)
                        }
                        showSuccessDialog()
                    } else {
                        val errorMessage = response.body()?.error ?: "Update failed: ${response.code()}"
                        Toast.makeText(this@ProfileActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                    binding.btnSaveChanges.isEnabled = true
                    Toast.makeText(this@ProfileActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(cacheDir, "temp_profile_photo.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showSuccessDialog() {
        val dialogBinding = DialogSuccessPopupBinding.inflate(LayoutInflater.from(this))
        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(dialogBinding.root)
        builder.setCancelable(false)
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }
}
