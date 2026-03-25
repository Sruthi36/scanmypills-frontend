package com.simats.scanmypills

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.simats.scanmypills.databinding.ActivityMedicineDetailsBinding
import com.simats.scanmypills.databinding.ItemMedicineReminderRowBinding
import com.simats.scanmypills.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Toast

data class Reminder(
    val id: String,
    val time: String,
    val dosage: String,
    var isActive: Boolean = true
)

@Suppress("DEPRECATION")
class MedicineDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicineDetailsBinding
    private val remindersList = mutableListOf<Reminder>()
    private var medicineId: Int = -1

    // Current medicine data state
    private var medicineName: String = ""
    private var manufacturer: String = ""
    private var expiryDate: String = ""
    private var batchNumber: String = ""
    private var dosageInfo: String = ""
    private var category: String = ""
    private var quantity: String = "20"
    private var frontUriString: String? = null
    private var backUriString: String? = null
    private var mainUriString: String? = null

    private val editMedicineLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            medicineName = data?.getStringExtra("medicine_name") ?: medicineName
            manufacturer = data?.getStringExtra("manufacturer") ?: manufacturer
            category = data?.getStringExtra("category") ?: category
            quantity = data?.getStringExtra("quantity") ?: quantity
            dosageInfo = data?.getStringExtra("dosage") ?: dosageInfo
            expiryDate = data?.getStringExtra("expiry_date") ?: expiryDate
            batchNumber = data?.getStringExtra("batch_number") ?: batchNumber
            mainUriString = data?.getStringExtra("main_image_uri") ?: mainUriString
            frontUriString = data?.getStringExtra("front_image_uri") ?: frontUriString
            backUriString = data?.getStringExtra("back_image_uri") ?: backUriString

            updateUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicineDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar setup
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Get initial data from intent
        medicineId = intent.getIntExtra("medicine_id", -1)
        medicineName = intent.getStringExtra("medicine_name") ?: "Medicine Name"
        manufacturer = intent.getStringExtra("manufacturer") ?: "Manufacturer"
        expiryDate = intent.getStringExtra("expiry_date") ?: "Not set"
        batchNumber = intent.getStringExtra("batch_number") ?: "Not set"
        dosageInfo = intent.getStringExtra("dosage") ?: "Not set"
        category = intent.getStringExtra("category") ?: "None"
        frontUriString = intent.getStringExtra("front_image_uri")
        backUriString = intent.getStringExtra("back_image_uri")
        mainUriString = intent.getStringExtra("main_image_uri") ?: frontUriString

        updateUI()

        if (medicineId != -1) {
            fetchMedicineDetails()
        }

        // Delete button logic
        binding.ivDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Edit Info click
        binding.btnEditInfo.setOnClickListener {
            val intent = Intent(this, EditMedicineActivity::class.java)
            intent.putExtra("medicine_id", medicineId)
            intent.putExtra("medicine_name", medicineName)
            intent.putExtra("manufacturer", manufacturer)
            intent.putExtra("category", category)
            intent.putExtra("quantity", quantity)
            intent.putExtra("dosage", dosageInfo)
            intent.putExtra("expiry_date", expiryDate)
            intent.putExtra("batch_number", batchNumber)
            intent.putExtra("main_image_uri", mainUriString)
            intent.putExtra("front_image_uri", frontUriString)
            intent.putExtra("back_image_uri", backUriString)
            editMedicineLauncher.launch(intent)
        }

        // Add reminder click
        binding.btnAddReminder.setOnClickListener {
            showAddReminderBottomSheet()
        }

        loadNavIcons()
        setupNavbar()
    }

    private fun showDeleteConfirmationDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(this).inflate(R.layout.layout_custom_dialog, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog.window?.attributes = layoutParams

        val iconContainer = view.findViewById<MaterialCardView>(R.id.iconContainer)
        val dialogIcon = view.findViewById<ImageView>(R.id.dialogIcon)
        val dialogTitle = view.findViewById<TextView>(R.id.dialogTitle)
        val dialogMessage = view.findViewById<TextView>(R.id.dialogMessage)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnConfirm)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)

        iconContainer.setCardBackgroundColor(Color.parseColor("#FEF2F2"))
        dialogIcon.setImageResource(R.drawable.ic_delete)
        dialogIcon.setColorFilter(Color.parseColor("#EF4444"))
        dialogTitle.text = "Delete Medicine"
        dialogMessage.text = "Are you sure you want to delete this medicine? This action cannot be undone."
        btnConfirm.text = "Delete"
        btnConfirm.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#EF4444"))

        btnConfirm.setOnClickListener {
            if (medicineId == -1) {
                dialog.dismiss()
                finish()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val token = prefs.getString("auth_token", null)
            if (token == null) {
                Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }

            RetrofitClient.instance.deleteMedicine("Bearer $token", medicineId)
                .enqueue(object : Callback<DeleteMedicineResponse> {
                    override fun onResponse(call: Call<DeleteMedicineResponse>, response: Response<DeleteMedicineResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(this@MedicineDetailsActivity, "Medicine deleted successfully", Toast.LENGTH_SHORT).show()
                            cancelAllLocalAlarms()
                            dialog.dismiss()
                            finish() 
                        } else {
                            val errorMsg = response.errorBody()?.string() ?: "Delete failed"
                            Toast.makeText(this@MedicineDetailsActivity, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<DeleteMedicineResponse>, t: Throwable) {
                        Toast.makeText(this@MedicineDetailsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun cancelAllLocalAlarms() {
        val rPrefs = getSharedPreferences("ScanMyPillsReminders", Context.MODE_PRIVATE)
        val alarmScheduler = AlarmScheduler(this)
        
        // Find all keys belonging to this medicine
        val keysToRemove = rPrefs.all.filter { 
            val value = it.value as? String ?: ""
            val parts = value.split("|")
            parts.size >= 4 && parts[2] == medicineName 
        }.keys

        keysToRemove.forEach { id ->
            alarmScheduler.cancelAlarm(id)
            rPrefs.edit().remove(id).apply()
        }
    }

    private fun formatTo12Hour(timeStr: String): String {
        if (timeStr.isEmpty()) return ""
        return try {
            val formats = arrayOf("HH:mm", "HH:mm:ss", "hh:mm a")
            var date: java.util.Date? = null
            for (f in formats) {
                try {
                    val sdf = java.text.SimpleDateFormat(f, java.util.Locale.US)
                    date = sdf.parse(timeStr)
                    if (date != null) break
                } catch (e: Exception) {}
            }
            if (date != null) {
                val outSdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
                outSdf.format(date).uppercase()
            } else timeStr
        } catch (e: Exception) { timeStr }
    }

    private fun updateUI() {
        binding.tvMedicineName.text = medicineName
        binding.tvManufacturer.text = manufacturer
        binding.tvDosage.text = dosageInfo
        binding.tvExpiryDate.text = expiryDate
        binding.tvBatchNumber.text = batchNumber
        binding.tvCategory.text = if (category.isNotEmpty()) category else "None"
        binding.tvCategory.visibility = if (category.isNotEmpty()) View.VISIBLE else View.GONE

        fun loadImage(uriStr: String?, imageView: ImageView) {
            uriStr?.let {
                val loadTarget = if (it.startsWith("content") || it.startsWith("file") || it.startsWith("http")) {
                    Uri.parse(it)
                } else {
                    RetrofitClient.getImageUrl(it)
                }
                Glide.with(this).load(loadTarget).placeholder(R.drawable.pill_placeholder).into(imageView)
            }
        }

        loadImage(mainUriString, binding.ivMainMedicine)
        loadImage(frontUriString, binding.ivPkgFront)
        loadImage(backUriString, binding.ivPkgBack)
    }

    private fun syncLocalReminders() {
        val rPrefs = getSharedPreferences("ScanMyPillsReminders", Context.MODE_PRIVATE)
        val editor = rPrefs.edit()
        
        // We only manage reminders for THIS medicine here to avoid clearing others
        val currentKeys = rPrefs.all.filter { 
            val value = it.value as? String ?: ""
            val parts = value.split("|")
            parts.size >= 4 && parts[2] == medicineName 
        }.keys
        currentKeys.forEach { editor.remove(it) }

        remindersList.forEach { r ->
            if (r.isActive) {
                editor.putString(r.id, "${r.id}|${r.time}|$medicineName|${r.dosage}")
            }
        }
        editor.apply()
        AlarmScheduler(this).rescheduleAllAlarms()
    }

    private fun fetchMedicineReminders() {
        if (medicineId == -1) return
        
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null) ?: return

        RetrofitClient.instance.getMedicineReminders("Bearer $token", medicineId)
            .enqueue(object : Callback<MedicineRemindersResponse> {
                override fun onResponse(call: Call<MedicineRemindersResponse>, response: Response<MedicineRemindersResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val remindersData = response.body()?.reminders
                        remindersList.clear()
                        remindersData?.forEach { r ->
                            remindersList.add(Reminder(
                                id = r.id.toString(),
                                time = r.reminderTime ?: "00:00",
                                dosage = r.dosage ?: "1 Tablet",
                                isActive = (r.isActive == 1)
                            ))
                        }
                        syncLocalReminders()
                        updateRemindersUI()
                    }
                }

                override fun onFailure(call: Call<MedicineRemindersResponse>, t: Throwable) {
                    Toast.makeText(this@MedicineDetailsActivity, "Sync failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchMedicineDetails() {
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null) ?: return

        RetrofitClient.instance.getMedicineDetails("Bearer $token", medicineId)
            .enqueue(object : Callback<MedicineDetailsResponse> {
                override fun onResponse(call: Call<MedicineDetailsResponse>, response: Response<MedicineDetailsResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val medicineData = response.body()?.medicine
                        val remindersData = response.body()?.reminders

                        medicineData?.let {
                            it.name?.let { medicineName = it }
                            it.manufacturer?.let { manufacturer = it }
                            it.expiryDate?.let { expiryDate = it }
                            it.batchNumber?.let { batchNumber = it }
                            it.dosage?.let { dosageInfo = it }
                            it.category?.let { category = it }
                            frontUriString = it.frontImage
                            backUriString = it.backImage
                            mainUriString = it.mainImage ?: it.frontImage
                            updateUI()
                        }

                            remindersData?.let { list ->
                                remindersList.clear()
                                list.forEach { r ->
                                    remindersList.add(Reminder(
                                        id = r.id.toString(),
                                        time = r.reminderTime ?: "00:00",
                                        dosage = r.dosage ?: "1 Tablet",
                                        isActive = (r.isActive == 1)
                                    ))
                                }
                                syncLocalReminders()
                                updateRemindersUI()
                            }
                    }
                }

                override fun onFailure(call: Call<MedicineDetailsResponse>, t: Throwable) {
                    Toast.makeText(this@MedicineDetailsActivity, "Error syncing: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showAddReminderBottomSheet() {
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val sheetBinding = com.simats.scanmypills.databinding.BottomSheetAddReminderBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(sheetBinding.root)

        var selectedTime = ""

        sheetBinding.tvSelectedTime.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val timePicker = android.app.TimePickerDialog(this, { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                val amPm = if (hour >= 12) "PM" else "AM"
                val hour12 = if (hour % 12 == 0) 12 else hour % 12
                sheetBinding.tvSelectedTime.text = String.format("%02d:%02d %s", hour12, minute, amPm)
                sheetBinding.tvSelectedTime.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), false)
            timePicker.show()
        }

        sheetBinding.btnSaveReminder.setOnClickListener {
            val dosage = sheetBinding.etDosageInput.text.toString().trim()

            if (selectedTime.isEmpty() || dosage.isEmpty()) {
                if (selectedTime.isEmpty()) sheetBinding.tvSelectedTime.error = "Required"
                if (dosage.isEmpty()) sheetBinding.etDosageInput.error = "Required"
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val token = prefs.getString("auth_token", null)
            if (token == null) {
                Toast.makeText(this@MedicineDetailsActivity, "Session expired", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = AddReminderRequest(
                medicineId = medicineId,
                reminderTime = selectedTime,
                dosage = dosage
            )

            RetrofitClient.instance.addReminder("Bearer $token", request)
                .enqueue(object : Callback<AddReminderResponse> {
                    override fun onResponse(call: Call<AddReminderResponse>, response: Response<AddReminderResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(this@MedicineDetailsActivity, "Reminder added successfully", Toast.LENGTH_SHORT).show()
                            
                            val returnedId = response.body()?.reminderId
                            if (returnedId != null) {
                                // Schedule immediately with the real ID
                                val alarmScheduler = AlarmScheduler(this@MedicineDetailsActivity)
                                alarmScheduler.scheduleAlarm(returnedId.toString(), selectedTime, medicineName, dosage)
                                
                                // Update local sync
                                val rPrefs = getSharedPreferences("ScanMyPillsReminders", Context.MODE_PRIVATE)
                                rPrefs.edit().putString(returnedId.toString(), "${returnedId}|${selectedTime}|$medicineName|$dosage").apply()
                            }

                            // Still refresh everything from server to be sure
                            fetchMedicineReminders()
                            bottomSheetDialog.dismiss()
                        } else {
                            val errorMsg = response.errorBody()?.string() ?: "Failed to add reminder"
                            Toast.makeText(this@MedicineDetailsActivity, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<AddReminderResponse>, t: Throwable) {
                        Toast.makeText(this@MedicineDetailsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        sheetBinding.btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun updateRemindersUI() {
        binding.tvNoReminders.visibility = if (remindersList.isEmpty()) View.VISIBLE else View.GONE
        binding.layoutRemindersList.removeAllViews()

        remindersList.sortBy { it.time }

        remindersList.forEach { reminder ->
            val itemBinding = ItemMedicineReminderRowBinding.inflate(LayoutInflater.from(this), binding.layoutRemindersList, false)
            itemBinding.tvReminderTime.text = formatTo12Hour(reminder.time)
            itemBinding.tvReminderDetails.text = reminder.dosage
            
            // Set initial toggle state
            itemBinding.ivToggle.setImageResource(if (reminder.isActive) R.drawable.ic_toggle_on else R.drawable.ic_toggle_off)
            itemBinding.viewStatus.setBackgroundResource(if (reminder.isActive) R.drawable.bg_dot_green else R.drawable.bg_dot_grey)

            itemBinding.ivToggle.setOnClickListener {
                val reminderIdInt = reminder.id.toIntOrNull()
                if (reminderIdInt != null) {
                    val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    val token = prefs.getString("auth_token", null)
                    
                    if (token != null) {
                        RetrofitClient.instance.toggleReminder("Bearer $token", reminderIdInt)
                            .enqueue(object : Callback<ToggleReminderResponse> {
                                override fun onResponse(call: Call<ToggleReminderResponse>, response: Response<ToggleReminderResponse>) {
                                    if (response.isSuccessful && response.body()?.success == true) {
                                        val newStatus = response.body()?.isActive == 1
                                        reminder.isActive = newStatus
                                        itemBinding.ivToggle.setImageResource(if (newStatus) R.drawable.ic_toggle_on else R.drawable.ic_toggle_off)
                                        itemBinding.viewStatus.setBackgroundResource(if (newStatus) R.drawable.bg_dot_green else R.drawable.bg_dot_grey)
                                        
                                        val alarmScheduler = AlarmScheduler(this@MedicineDetailsActivity)
                                        val rPrefs = getSharedPreferences("ScanMyPillsReminders", Context.MODE_PRIVATE)
                                        
                                        if (newStatus) {
                                            alarmScheduler.scheduleAlarm(reminder.id, reminder.time, medicineName, reminder.dosage)
                                            rPrefs.edit().putString(reminder.id, "${reminder.id}|${reminder.time}|$medicineName|${reminder.dosage}").apply()
                                            Toast.makeText(this@MedicineDetailsActivity, "Reminder activated", Toast.LENGTH_SHORT).show()
                                        } else {
                                            alarmScheduler.cancelAlarm(reminder.id)
                                            rPrefs.edit().remove(reminder.id).apply()
                                            Toast.makeText(this@MedicineDetailsActivity, "Reminder deactivated", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                override fun onFailure(call: Call<ToggleReminderResponse>, t: Throwable) {
                                    Toast.makeText(this@MedicineDetailsActivity, "Network error", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }
            }
            
            itemBinding.ivDeleteReminder.setOnClickListener {
                val reminderIdInt = reminder.id.toIntOrNull()
                
                if (reminderIdInt != null) {
                    val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    val token = prefs.getString("auth_token", null)
                    
                    if (token != null) {
                        RetrofitClient.instance.deleteReminder("Bearer $token", reminderIdInt)
                            .enqueue(object : Callback<DeleteReminderResponse> {
                                override fun onResponse(call: Call<DeleteReminderResponse>, response: Response<DeleteReminderResponse>) {
                                    if (response.isSuccessful && response.body()?.success == true) {
                                        Toast.makeText(this@MedicineDetailsActivity, "Reminder deleted", Toast.LENGTH_SHORT).show()
                                        performLocalDeletion(reminder)
                                    } else {
                                        val errorMsg = response.errorBody()?.string() ?: "Delete failed"
                                        Toast.makeText(this@MedicineDetailsActivity, errorMsg, Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<DeleteReminderResponse>, t: Throwable) {
                                    Toast.makeText(this@MedicineDetailsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                    } else {
                        performLocalDeletion(reminder)
                    }
                } else {
                    // It's a local UUID, possibly added but not synced yet
                    performLocalDeletion(reminder)
                }
            }
            
            binding.layoutRemindersList.addView(itemBinding.root)
        }
    }

    private fun performLocalDeletion(reminder: Reminder) {
        // Remove from shared prefs
        val prefs = getSharedPreferences("ScanMyPillsReminders", Context.MODE_PRIVATE)
        prefs.edit().remove(reminder.id).apply()
        
        // Cancel alarm
        val alarmScheduler = AlarmScheduler(this@MedicineDetailsActivity)
        alarmScheduler.cancelAlarm(reminder.id)

        remindersList.remove(reminder)
        updateRemindersUI()
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
