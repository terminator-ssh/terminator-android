package com.terminatorssh.terminator.data.remote

import com.google.gson.Gson
import com.terminatorssh.terminator.data.remote.service.TerminatorApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClientFactory(private val gson: Gson) {
    fun create(baseUrl: String): TerminatorApiService {
        val validUrl =
            if (baseUrl.endsWith("/"))
                baseUrl
            else
                "$baseUrl/"

        return Retrofit.Builder()
            .baseUrl(validUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(TerminatorApiService::class.java)
    }
}