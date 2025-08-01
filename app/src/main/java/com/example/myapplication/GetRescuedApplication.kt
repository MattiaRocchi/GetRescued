package com.example.myapplication



import android.app.Application
import org.koin.core.context.startKoin
import org.koin.android.ext.koin.androidContext
import com.example.myapplication.appModule

class GetRescuedApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GetRescuedApplication)
            modules(appModule)
        }
    }
}