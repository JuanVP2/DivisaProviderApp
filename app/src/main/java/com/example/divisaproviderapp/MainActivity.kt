package com.example.divisaproviderapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Button(onClick = {
                (application as DivisaApplication).forzarSincronizacionInmediata()
            }) {
                Text("Forzar Sincronización")
                val appInstance = application
                Log.d("MainActivity", "Tipo de application: ${appInstance::class.java}")
                val divisaApp = application as DivisaApplication
            }
        }
    }
}

