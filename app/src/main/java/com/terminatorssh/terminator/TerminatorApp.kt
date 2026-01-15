package com.terminatorssh.terminator

import android.app.Application
import com.terminatorssh.terminator.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class TerminatorApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@TerminatorApp)
            modules(appModule)
        }
    }
}