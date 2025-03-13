package com.example.divisainterfaz

import android.app.Application
import android.util.Log

class DivisaClientApplication : Application() {

    lateinit var divisaRepository: DivisaClientRepository

    override fun onCreate() {
        super.onCreate()

        divisaRepository = DivisaClientRepository(this)

        Log.d("DivisaClientApp", "Application initialized - ready to fetch data from content provider")
    }
}
