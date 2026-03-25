package com.simats.scanmypills

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simats.scanmypills.databinding.ActivityScanReviewBinding
import com.simats.scanmypills.network.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

@Suppress("DEPRECATION")
class ScanReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanReviewBinding
    private var frontUri: Uri? = null
    private var backUri: Uri? = null
    private lateinit var progressDialog: androidx.appcompat.app.AlertDialog
    private var processedFrontFilename: String? = null
    private var processedBackFilename: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar setup
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
             navigateBack()
        }

        binding.btnBack.setOnClickListener {
             navigateBack()
        }

        val frontImageUriStr = intent.getStringExtra("front_image_uri")
        val backImageUriStr = intent.getStringExtra("back_image_uri")
        
        frontUri = frontImageUriStr?.let { Uri.parse(it) }
        backUri = backImageUriStr?.let { Uri.parse(it) }

        // Initial processing and previews
        if (frontUri != null && backUri != null) {
            Glide.with(this).load(frontUri).into(binding.ivReviewFront)
            Glide.with(this).load(backUri).into(binding.ivReviewBack)
            processImages(frontUri!!, backUri!!)
        } else {
            Toast.makeText(this, "Error: Missing image data from scanning step.", Toast.LENGTH_LONG).show()
        }

        binding.etExpiryDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etMedicineName.text.toString().trim()
            if (name.isEmpty()) {
                binding.etMedicineName.error = "Medicine name is required"
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val token = prefs.getString("auth_token", null)
            if (token == null) {
                Toast.makeText(this, "Session expired, please login again", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Prepare request
            val request = SaveMedicineRequest(
                name = name,
                manufacturer = binding.etManufacturer.text.toString().trim(),
                expiryDate = binding.etExpiryDate.text.toString().trim(),
                batchNumber = binding.etBatchNumber.text.toString().trim(),
                mrp = 0.0, // Default as it's not in the UI yet
                dosage = binding.etDosageInstructions.text.toString().trim(),
                category = binding.etCategory.text.toString().trim(),
                quantity = 10, // Default
                frontImage = processedFrontFilename,
                backImage = processedBackFilename,
                mainImage = processedFrontFilename
            )

            showLoading(true, "Saving medicine...")

            RetrofitClient.instance.saveMedicine("Bearer $token", request)
                .enqueue(object : Callback<SaveMedicineResponse> {
                    override fun onResponse(call: Call<SaveMedicineResponse>, response: Response<SaveMedicineResponse>) {
                        showLoading(false)
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(this@ScanReviewActivity, "Medicine saved successfully!", Toast.LENGTH_SHORT).show()
                            
                            val intent = Intent(this@ScanReviewActivity, MedicineDetailsActivity::class.java)
                            intent.putExtra("medicine_id", response.body()?.medicineId)
                            intent.putExtra("medicine_name", name)
                            intent.putExtra("manufacturer", request.manufacturer)
                            intent.putExtra("expiry_date", request.expiryDate)
                            intent.putExtra("batch_number", request.batchNumber)
                            intent.putExtra("dosage", request.dosage)
                            intent.putExtra("category", request.category)
                            intent.putExtra("main_image_uri", processedFrontFilename)
                            intent.putExtra("front_image_uri", processedFrontFilename)
                            intent.putExtra("back_image_uri", processedBackFilename)
                            startActivity(intent)
                            finish()
                        } else {
                            val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                            Toast.makeText(this@ScanReviewActivity, "Save failed: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<SaveMedicineResponse>, t: Throwable) {
                        showLoading(false)
                        Toast.makeText(this@ScanReviewActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }

        loadNavIcons()
        setupNavbar()
    }

    private fun processImages(front: Uri, back: Uri) {
        showLoading(true, "Extracting details using AI, please wait...")
        
        val frontFile = getFileFromUri(front, "front_temp.jpg")
        val backFile = getFileFromUri(back, "back_temp.jpg")
        
        if (frontFile == null || backFile == null) {
            showLoading(false)
            Toast.makeText(this, "Failed to prepare images", Toast.LENGTH_SHORT).show()
            return
        }

        val frontBody = RequestBody.create(MediaType.parse("image/*"), frontFile)
        val backBody = RequestBody.create(MediaType.parse("image/*"), backFile)
        
        val frontPart = MultipartBody.Part.createFormData("front_image", frontFile.name, frontBody)
        val backPart = MultipartBody.Part.createFormData("back_image", backFile.name, backBody)

        RetrofitClient.instance.processMedicine(frontPart, backPart)
            .enqueue(object : Callback<MedicineProcessResponse> {
                override fun onResponse(call: Call<MedicineProcessResponse>, response: Response<MedicineProcessResponse>) {
                    showLoading(false)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()?.data
                        data?.let {
                            binding.etMedicineName.setText(it.name ?: "Unknown Medicine")
                            binding.etManufacturer.setText(it.manufacturer ?: "Unknown Manufacturer")
                            binding.etExpiryDate.setText(it.expiryDate ?: "")
                            binding.etBatchNumber.setText(it.batchNumber ?: "")
                            binding.etCategory.setText("General")
                            
                            processedFrontFilename = it.frontImage
                            processedBackFilename = it.backImage
                            
                            binding.successBanner.visibility = View.VISIBLE
                        }
                        Toast.makeText(this@ScanReviewActivity, "Scan complete!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ScanReviewActivity, "Processing failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MedicineProcessResponse>, t: Throwable) {
                    showLoading(false)
                    Toast.makeText(this@ScanReviewActivity, "OCR error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun showLoading(show: Boolean, message: String = "Please wait...") {
        if (!::progressDialog.isInitialized) {
            progressDialog = MaterialAlertDialogBuilder(this)
                .setTitle("Processing")
                .setMessage(message)
                .setCancelable(false)
                .create()
        } else {
            progressDialog.setMessage(message)
        }
        
        if (show) progressDialog.show() else progressDialog.dismiss()
    }

    private fun getFileFromUri(uri: Uri, name: String): File? {
        return try {
            val contentResolver = applicationContext.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(cacheDir, name)
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showDatePicker() {
        val calendar = java.util.Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                binding.etExpiryDate.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth))
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun navigateBack() {
        val intent = Intent(this, ScanPillActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
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
        }

        binding.navScan.setOnClickListener {
            val intent = Intent(this, ScanPillActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        binding.navIdentify.setOnClickListener {
            val intent = Intent(this, IdentifyPillActivity::class.java)
            startActivity(intent)
        }

        binding.navReminders.setOnClickListener {
            val intent = Intent(this, RemindersActivity::class.java)
            startActivity(intent)
        }

        binding.navProfile.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
