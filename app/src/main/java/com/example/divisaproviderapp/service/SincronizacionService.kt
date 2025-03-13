package com.example.divisaproviderapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.divisaproviderapp.R
import com.example.divisaproviderapp.workers.ActualizarDivisasWorker

class SincronizacionService : Service() {

    override fun onCreate() {
        super.onCreate()
        iniciarCanalNotificacion()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, "sync_channel")
            .setContentTitle("Sincronización activa")
            .setContentText("Actualizando divisas en segundo plano...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification) // Mantiene el servicio activo

        // Ejecutar WorkManager para actualizar la BD
        val workRequest = OneTimeWorkRequestBuilder<ActualizarDivisasWorker>().build()
        WorkManager.getInstance(this).enqueue(workRequest)

        return START_STICKY
    }

    private fun iniciarCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "sync_channel",
                "Sincronización de Divisas",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
