package com.simats.scanmypills.network

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("profile_photo") val profilePhoto: String?,
    @SerializedName("created_at") val createdAt: String?
)
