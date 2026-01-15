package com.terminatorssh.terminator.di

import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.terminatorssh.terminator.data.remote.RetrofitClientFactory
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    single { GsonBuilder().setStrictness(Strictness.LENIENT).create() }
    single { RetrofitClientFactory(get()) }
//    single {
//        Retrofit.Builder()
//            .baseUrl("http://192.168.100.10:5000/api/v1")
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(get())
//            .build()
//    }
}