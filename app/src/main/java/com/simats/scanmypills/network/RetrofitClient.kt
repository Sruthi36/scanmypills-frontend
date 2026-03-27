package com.simats.scanmypills.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // 10.0.2.2 is the special IP to access localhost from Android Emulator
    const val BASE_URL = "http://180.235.121.253:8115/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }

    fun getImageUrl(imagePath: String?): String? {
        if (imagePath.isNullOrEmpty()) return null
        
        // If it's already a full URL or a local URI, return it as is
        if (imagePath.startsWith("http") || 
            imagePath.startsWith("content://") || 
            imagePath.startsWith("file://")) {
            return imagePath
        }
        
        val cleanPath = imagePath.trimStart('/')
        val base = BASE_URL.trimEnd('/')
        
        // Check if path already starts with uploads/ to avoid duplication
        return if (cleanPath.startsWith("uploads/")) {
            "$base/$cleanPath"
        } else {
            "$base/uploads/$cleanPath"
        }
    }
}
