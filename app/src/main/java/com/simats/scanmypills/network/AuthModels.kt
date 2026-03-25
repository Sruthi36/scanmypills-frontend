package com.simats.scanmypills.network

import com.google.gson.annotations.SerializedName

data class VerifyOtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String
)

data class VerifyOtpResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: String?
)

data class ResendOtpRequest(
    @SerializedName("email") val email: String
)

data class ResendOtpResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: String?
)

data class ResetPassRequest(
    @SerializedName("email") val email: String,
    @SerializedName("new_password") val newPassword: String
)

data class ResetPassResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: String?
)

data class GeneralResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: String?
)

data class ChangePassRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("confirm_password") val confirmPassword: String
)
