package com.simats.scanmypills.network

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("error") val error: String?
)
