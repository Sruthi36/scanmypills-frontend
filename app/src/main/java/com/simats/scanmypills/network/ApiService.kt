package com.simats.scanmypills.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/auth/forgot-password")
    fun forgotPassword(@Body request: ForgotPassRequest): Call<ForgotPassResponse>

    @POST("api/auth/verify-otp")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<VerifyOtpResponse>

    @POST("api/auth/resend-otp")
    fun resendOtp(@Body request: ResendOtpRequest): Call<ResendOtpResponse>

    @POST("api/auth/reset-password")
    fun resetPassword(@Body request: ResetPassRequest): Call<ResetPassResponse>

    @GET("api/user/{user_id}")
    fun getUserProfile(
        @Header("Authorization") token: String,
        @Path("user_id") userId: Int
    ): Call<User>

    @Multipart
    @PUT("api/user/{user_id}")
    fun updateUserProfile(
        @Header("Authorization") token: String,
        @Path("user_id") userId: Int,
        @Part("name") name: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part profilePhoto: MultipartBody.Part?
    ): Call<GeneralResponse>

    @PUT("api/user/change-password")
    fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePassRequest
    ): Call<GeneralResponse>

    @DELETE("api/delete-account")
    fun deleteAccount(
        @Header("Authorization") token: String
    ): Call<GeneralResponse>

    @Multipart
    @POST("api/process-medicine")
    fun processMedicine(
        @Part front_image: MultipartBody.Part,
        @Part back_image: MultipartBody.Part
    ): Call<MedicineProcessResponse>

    @POST("api/medicines")
    fun saveMedicine(
        @Header("Authorization") token: String,
        @Body request: SaveMedicineRequest
    ): Call<SaveMedicineResponse>

    @GET("api/medicines/{id}")
    fun getMedicineDetails(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<MedicineDetailsResponse>

    @DELETE("api/medicines/{id}")
    fun deleteMedicine(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<DeleteMedicineResponse>

    @Multipart
    @PUT("api/medicines/{id}")
    fun updateMedicine(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Part("medicine_name") name: RequestBody?,
        @Part("manufacturer") manufacturer: RequestBody?,
        @Part("category") category: RequestBody?,
        @Part("quantity") quantity: RequestBody?,
        @Part("dosage") dosage: RequestBody?,
        @Part("expiry_date") expiryDate: RequestBody?,
        @Part("batch_number") batchNumber: RequestBody?,
        @Part main_image: MultipartBody.Part?,
        @Part front_image: MultipartBody.Part?,
        @Part back_image: MultipartBody.Part?
    ): Call<UpdateMedicineResponse>

    @GET("api/medicines")
    fun getAllMedicines(
        @Header("Authorization") token: String
    ): Call<AllMedicinesResponse>

    @GET("api/identify-medicine")
    fun identifyMedicine(
        @Header("Authorization") token: String,
        @Query("name") name: String
    ): Call<IdentifyMedicineResponse>

    @POST("api/reminders")
    fun addReminder(
        @Header("Authorization") token: String,
        @Body request: AddReminderRequest
    ): Call<AddReminderResponse>

    @DELETE("api/reminders/{reminder_id}")
    fun deleteReminder(
        @Header("Authorization") token: String,
        @Path("reminder_id") reminderId: Int
    ): Call<DeleteReminderResponse>

    @PUT("api/reminders/{reminder_id}/toggle")
    fun toggleReminder(
        @Header("Authorization") token: String,
        @Path("reminder_id") reminderId: Int
    ): Call<ToggleReminderResponse>

    @GET("api/reminders/today")
    fun getSchedule(
        @Header("Authorization") token: String
    ): Call<ScheduleResponse>

    @GET("api/reminders")
    fun getAllRemindersGlobal(
        @Header("Authorization") token: String
    ): Call<GlobalRemindersResponse>

    @GET("api/medicines/{medicine_id}/reminders")
    fun getMedicineReminders(
        @Header("Authorization") token: String,
        @Path("medicine_id") medicineId: Int
    ): Call<MedicineRemindersResponse>
}
