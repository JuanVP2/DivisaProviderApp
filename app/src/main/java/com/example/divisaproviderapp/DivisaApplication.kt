package com.example.divisaproviderapp

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.work.Constraints
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.divisaproviderapp.data.DivisaDatabase
import com.example.divisaproviderapp.data.DivisaRepository
import com.example.divisaproviderapp.data.NetworkDivisaRepository
import com.example.divisaproviderapp.network.DivisaApiService
import com.example.divisaproviderapp.service.SincronizacionService
import com.example.divisaproviderapp.workers.ActualizarDivisasWorker
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class DivisaApplication : Application(), Configuration.Provider {

    lateinit var repository: DivisaRepository

    override fun onCreate() {
        super.onCreate()
        forzarSincronizacionInmediata()
        val intent = Intent(this, SincronizacionService::class.java)
        startService(intent)
        try {
            val db = DivisaDatabase.getInstance(this)

            val baseUrl = "https://v6.exchangerate-api.com/v6/ea89034455f0918eb70bb598/"
            val retrofit = Retrofit.Builder()
                .addConverterFactory(
                    Json { ignoreUnknownKeys = true }
                        .asConverterFactory("application/json".toMediaType())
                )
                .baseUrl(baseUrl)
                .build()

            val apiService = retrofit.create(DivisaApiService::class.java)

            repository = NetworkDivisaRepository(apiService, db.divisaDao())

            programarWorkManager()

            //forzarSincronizacionInmediata()

            Log.d("DivisaApplication", "Provider App inicializado correctamente.")
        } catch (e: Exception) {
            Log.e("DivisaApplication", "Error al inicializar la app Provider", e)
        }
    }

    fun forzarSincronizacionInmediata() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeRequest = OneTimeWorkRequestBuilder<ActualizarDivisasWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueue(oneTimeRequest)
        Log.d("DivisaApplication", "Sincronización inmediata encolada.")
    }

    private fun programarWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ActualizarDivisasWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DivisaSyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    // Configuración de WorkManager
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
    }
}
