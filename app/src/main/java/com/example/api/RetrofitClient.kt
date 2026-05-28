package com.example.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String? = null

    fun getApi(baseUrl: String): XtreamApi {
        val serverUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        if (retrofit == null || currentBaseUrl != serverUrl) {
            currentBaseUrl = serverUrl
            retrofit = Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(XtreamApi::class.java)
    }
}
