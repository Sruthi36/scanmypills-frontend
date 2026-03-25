package com.simats.scanmypills.network

import com.google.gson.annotations.SerializedName

data class ForgotPassResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("seconds_remaining") val secondsRemaining: Int?
)
