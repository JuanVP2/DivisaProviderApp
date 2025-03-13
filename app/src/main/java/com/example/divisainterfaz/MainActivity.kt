package com.example.divisainterfaz

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.divisainterfaz.ui.theme.DivisaInterfazTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as DivisaClientApplication).divisaRepository
        Log.d("MainActivity", "Activity created - repository initialized")

        setContent {
            DivisaInterfazTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: DivisaChartViewModel = viewModel(
                        factory = DivisaChartViewModel.Factory(repository)
                    )

                    DivisaChartScreen(viewModel)
                }
            }
        }
    }
}