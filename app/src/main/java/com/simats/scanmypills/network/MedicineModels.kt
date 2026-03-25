package com.simats.scanmypills.network

import com.google.gson.annotations.SerializedName

data class MedicineProcessResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: MedicineData?
)

data class MedicineData(
    @SerializedName("id") val id: Int?, // Add id
    @SerializedName("name") val name: String?,
    @SerializedName("manufacturer") val manufacturer: String?, // Add manufacturer
    @SerializedName("expiry_date") val expiryDate: String?,
    @SerializedName("batch_number") val batchNumber: String?,
    @SerializedName("mrp") val mrp: String?,
    @SerializedName("dosage") val dosage: String?, // Add dosage
    @SerializedName("category") val category: String?, // Add category
    @SerializedName("quantity") val quantity: Int?, // Add quantity
    @SerializedName("main_image") val mainImage: String?, // Add main_image
    @SerializedName("front_image") val frontImage: String?,
    @SerializedName("back_image") val backImage: String?
)

data class SaveMedicineRequest(
    @SerializedName("name") val name: String,
    @SerializedName("manufacturer") val manufacturer: String?,
    @SerializedName("expiry_date") val expiryDate: String?,
    @SerializedName("batch_number") val batchNumber: String?,
    @SerializedName("mrp") val mrp: Double?,
    @SerializedName("dosage") val dosage: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("quantity") val quantity: Int?,
    @SerializedName("front_image") val frontImage: String?,
    @SerializedName("back_image") val backImage: String?,
    @SerializedName("main_image") val mainImage: String?
)

data class SaveMedicineResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("medicine_id") val medicineId: Int?
)

data class MedicineDetailsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("medicine") val medicine: MedicineData,
    @SerializedName("reminders") val reminders: List<ReminderResponse>
)

data class ReminderResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("reminder_time") val reminderTime: String?,
    @SerializedName("dosage") val dosage: String?,
    @SerializedName("is_active") val isActive: Int?
)

data class DeleteMedicineResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("medicine_id") val medicineId: Int?
)

data class UpdateMedicineResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

data class AddReminderRequest(
    @SerializedName("medicine_id") val medicineId: Int,
    @SerializedName("reminder_time") val reminderTime: String,
    @SerializedName("dosage") val dosage: String
)

data class AddReminderResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("reminder_id") val reminderId: Int?
)

data class DeleteReminderResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

data class ToggleReminderResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("is_active") val isActive: Int
)

data class AllMedicinesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("count") val count: Int,
    @SerializedName("medicines") val medicines: List<MedicineData>
)

data class IdentifyMatch(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("manufacturer") val manufacturer: String?,
    @SerializedName("dosage") val dosage: String?,
    @SerializedName("expiry_date") val expiryDate: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("main_image") val mainImage: String?,
    @SerializedName("front_image") val frontImage: String?,
    @SerializedName("back_image") val backImage: String?
)

data class IdentifyMedicineResponse(
    @SerializedName("status") val status: String,
    @SerializedName("entered_name") val enteredName: String,
    @SerializedName("match_count") val match_count: Int,
    @SerializedName("matches") val matches: List<IdentifyMatch>,
    @SerializedName("suggestions") val suggestions: List<String>
)

data class ScheduleItem(
    @SerializedName("id") val id: Int,
    @SerializedName("time") val time: String,
    @SerializedName("medicine_name") val medicineName: String,
    @SerializedName("dosage") val dosage: String,
    @SerializedName("status") val status: String
)

data class ScheduleResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("schedule") val schedule: List<ScheduleItem>
)

data class GlobalRemindersResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("reminders") val reminders: List<MedicineReminders>
)

data class MedicineReminders(
    @SerializedName("medicine_name") val medicineName: String,
    @SerializedName("main_image") val mainImage: String?,
    @SerializedName("items") val items: List<ReminderResponse>
)

data class MedicineRemindersResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("reminders") val reminders: List<ReminderResponse>
)
