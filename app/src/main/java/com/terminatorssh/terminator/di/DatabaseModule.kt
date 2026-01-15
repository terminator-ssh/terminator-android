package com.terminatorssh.terminator.di

import androidx.room.Room
import com.terminatorssh.terminator.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
                androidContext(),
                AppDatabase::class.java,
                "terminator.db"
            ).fallbackToDestructiveMigration(true).build()
    }
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().blobDao() }
}