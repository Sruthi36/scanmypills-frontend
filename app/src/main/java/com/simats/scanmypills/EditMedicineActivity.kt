package com.simats.scanmypills

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.simats.scanmypills.databinding.ActivityEditMedicineBinding
import android.view.View
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simats.scanmypills.network.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

@Suppress("DEPRECATION")
class EditMedicineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditMedicineBinding
    private lateinit var progressDialog: androidx.appcompat.app.AlertDialog
    
    private var medicineId: Int = -1
    private var tabletImageUri: Uri? = null
    private var frontImageUri: Uri? = null
    private var backImageUri: Uri? = null

    // Tracking if new images were actually picked (URIs from intent might be content:// or full URLs depending on implementation)
    // Actually, the intent passes them as strings. Let's track if they are changed.
    private var isTabletImageChanged = false
    private var isFrontImageChanged = false
    private var isBackImageChanged = false

    private val tabletImagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            tabletImageUri = result.data?.data
            isTabletImageChanged = true
            Glide.with(this).load(tabletImageUri).into(binding.ivTabletImage)
        }
    }

    private val frontImagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            frontImageUri = result.data?.data
            isFrontImageChanged = true
            Glide.with(this).load(frontImageUri).into(binding.ivFrontImage)
        }
    }

    private val backImagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            backImageUri = result.data?.data
            isBackImageChanged = true
            Glide.with(this).load(backImageUri).into(binding.ivBackImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditMedicineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadInitialData()
        setupListeners()
        loadNavIcons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
             onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadInitialData() {
        medicineId = intent.getIntExtra("medicine_id", -1)
        binding.etMedicineName.setText(intent.getStringExtra("medicine_name"))
        binding.etManufacturer.setText(intent.getStringExtra("manufacturer"))
        binding.etCategory.setText(intent.getStringExtra("category"))
        binding.etQuantity.setText(intent.getStringExtra("quantity") ?: "20")
        binding.etDosage.setText(intent.getStringExtra("dosage"))
        binding.etExpiryDate.setText(intent.getStringExtra("expiry_date"))
        binding.etBatchNumber.setText(intent.getStringExtra("batch_number"))

        val mainImg = intent.getStringExtra("main_image_uri")
        val frontImg = intent.getStringExtra("front_image_uri")
        val backImg = intent.getStringExtra("back_image_uri")

        fun loadImage(uriStr: String?, imageView: android.widget.ImageView) {
            uriStr?.let {
                val loadTarget = if (it.startsWith("content") || it.startsWith("file") || it.startsWith("http")) {
                    Uri.parse(it)
                } else {
                    RetrofitClient.getImageUrl(it)
                }
                Glide.with(this).load(loadTarget).placeholder(R.drawable.pill_placeholder).into(imageView)
            }
        }

        mainImg?.let { 
            tabletImageUri = Uri.parse(it)
            loadImage(it, binding.ivTabletImage)
        }
        frontImg?.let { 
            frontImageUri = Uri.parse(it)
            loadImage(it, binding.ivFrontImage)
        }
        backImg?.let { 
            backImageUri = Uri.parse(it)
            loadImage(it, binding.ivBackImage)
        }
    }

    private fun setupListeners() {
        binding.etExpiryDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnChangeTabletImage.setOnClickListener {
            openImagePicker(tabletImagePicker)
        }

        binding.btnChangeFrontImage.setOnClickListener {
            openImagePicker(frontImagePicker)
        }

        binding.btnChangeBackImage.setOnClickListener {
            openImagePicker(backImagePicker)
        }

        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }
        
        setupNavbar()
    }

    private fun showDatePicker() {
        val calendar = java.util.Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                binding.etExpiryDate.setText(String.format("%02d/%02d/%d", dayOfMonth, month + 1, year))
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun openImagePicker(launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        launcher.launch(intent)
    }

    private fun saveChanges() {
        val name = binding.etMedicineName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etMedicineName.error = "Name required"
            return
        }

        if (medicineId == -1) {
            Toast.makeText(this, "Error: Invalid Medicine ID", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)
        if (token == null) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true, "Updating medicine...")

        // Prepare Multipart Parts
        val nameBody = RequestBody.create(MediaType.parse("text/plain"), name)
        val manufacturerBody = RequestBody.create(MediaType.parse("text/plain"), binding.etManufacturer.text.toString())
        val categoryBody = RequestBody.create(MediaType.parse("text/plain"), binding.etCategory.text.toString())
        val quantityBody = RequestBody.create(MediaType.parse("text/plain"), binding.etQuantity.text.toString())
        val dosageBody = RequestBody.create(MediaType.parse("text/plain"), binding.etDosage.text.toString())
        val expiryDateBody = RequestBody.create(MediaType.parse("text/plain"), binding.etExpiryDate.text.toString())
        val batchNumberBody = RequestBody.create(MediaType.parse("text/plain"), binding.etBatchNumber.text.toString())

        var mainImgPart: MultipartBody.Part? = null
        var frontImgPart: MultipartBody.Part? = null
        var backImgPart: MultipartBody.Part? = null

        if (isTabletImageChanged) {
            tabletImageUri?.let { uri ->
                getFileFromUri(uri, "update_main.jpg")?.let { file ->
                    val body = RequestBody.create(MediaType.parse("image/*"), file)
                    mainImgPart = MultipartBody.Part.createFormData("main_image", file.name, body)
                }
            }
        }

        if (isFrontImageChanged) {
            frontImageUri?.let { uri ->
                getFileFromUri(uri, "update_front.jpg")?.let { file ->
                    val body = RequestBody.create(MediaType.parse("image/*"), file)
                    frontImgPart = MultipartBody.Part.createFormData("front_image", file.name, body)
                }
            }
        }

        if (isBackImageChanged) {
            backImageUri?.let { uri ->
                getFileFromUri(uri, "update_back.jpg")?.let { file ->
                    val body = RequestBody.create(MediaType.parse("image/*"), file)
                    backImgPart = MultipartBody.Part.createFormData("back_image", file.name, body)
                }
            }
        }

        RetrofitClient.instance.updateMedicine(
            "Bearer $token", medicineId,
            nameBody, manufacturerBody, categoryBody, quantityBody, dosageBody, expiryDateBody, batchNumberBody,
            mainImgPart, frontImgPart, backImgPart
        ).enqueue(object : Callback<UpdateMedicineResponse> {
            override fun onResponse(call: Call<UpdateMedicineResponse>, response: Response<UpdateMedicineResponse>) {
                showLoading(false)
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@EditMedicineActivity, "Updated successfully", Toast.LENGTH_SHORT).show()
                    
                    val resultIntent = Intent()
                    resultIntent.putExtra("medicine_name", name)
                    resultIntent.putExtra("manufacturer", binding.etManufacturer.text.toString())
                    resultIntent.putExtra("category", binding.etCategory.text.toString())
                    resultIntent.putExtra("quantity", binding.etQuantity.text.toString())
                    resultIntent.putExtra("dosage", binding.etDosage.text.toString())
                    resultIntent.putExtra("expiry_date", binding.etExpiryDate.text.toString())
                    resultIntent.putExtra("batch_number", binding.etBatchNumber.text.toString())
                    
                    tabletImageUri?.let { resultIntent.putExtra("main_image_uri", it.toString()) }
                    frontImageUri?.let { resultIntent.putExtra("front_image_uri", it.toString()) }
                    backImageUri?.let { resultIntent.putExtra("back_image_uri", it.toString()) }

                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this@EditMedicineActivity, "Update failed: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<UpdateMedicineResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(this@EditMedicineActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
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
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(cacheDir, name)
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
