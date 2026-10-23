package com.kieling.groceries

import android.app.Application
import com.kieling.groceries.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class GroceriesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@GroceriesApplication)
            modules(appModule)
        }
    }
}
