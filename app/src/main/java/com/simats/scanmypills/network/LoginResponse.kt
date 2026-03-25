package com.simats.scanmypills.network

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("token") val token: String?,
    @SerializedName("user") val user: UserProfile?,
    @SerializedName("error") val error: String?
)

data class UserProfile(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String
)
