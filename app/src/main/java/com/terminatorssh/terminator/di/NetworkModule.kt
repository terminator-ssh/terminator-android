package com.terminatorssh.terminator.di

import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.terminatorssh.terminator.data.remote.RetrofitClientFactory
import org.koin.dsl.module

val networkModule = module {
    single { GsonBuilder().setStrictness(Strictness.LENIENT).create() }
    single { RetrofitClientFactory(get()) }
}