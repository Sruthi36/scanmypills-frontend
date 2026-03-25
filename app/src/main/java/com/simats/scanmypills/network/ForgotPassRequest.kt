package com.simats.scanmypills.network

import com.google.gson.annotations.SerializedName

data class ForgotPassRequest(
    @SerializedName("email") val email: String
)
