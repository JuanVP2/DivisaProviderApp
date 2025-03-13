package com.example.divisainterfaz

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.divisainterfaz.ui.theme.DivisaInterfazTheme
import com.google.android.libraries.intelligence.acceleration.Analytics

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
                    val viewModel: DivisaViewModel = viewModel(
                        factory = DivisaViewModel.Factory(repository)
                    )

                    val chartViewModel: DivisaChartViewModel = viewModel(
                        factory = DivisaChartViewModel.Factory(repository)
                    )

                    // Track which screen is currently active
                    var currentScreen by remember { mutableStateOf(Screen.LIST) }

                    Scaffold(
                        bottomBar = {
                            BottomAppBar {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    IconButton(onClick = { currentScreen = Screen.LIST }) {
                                        Icon(
                                            imageVector = Icons.Default.List,
                                            contentDescription = "Lista"
                                        )
                                    }

                                    IconButton(onClick = { currentScreen = Screen.CHART }) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Gráfico"
                                        )
                                    }
                                }
                            }
                        }
                    ) { paddingValues ->
                        Column(modifier = Modifier.padding(paddingValues)) {
                            when (currentScreen) {
                                Screen.LIST -> DivisaScreen(viewModel)
                                Screen.CHART -> DivisaChartScreen(chartViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Enum para controlar la navegación
enum class Screen {
    LIST,
    CHART
}

@Composable
fun DivisaScreen(viewModel: DivisaViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Divisa Client App",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Currency selection buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { viewModel.loadUsdRates() }) {
                Text("USD Rates")
            }

            Button(onClick = { viewModel.loadEurRates() }) {
                Text("EUR Rates")
            }

            Button(onClick = { viewModel.loadTodayRates(uiState.selectedCurrency) }) {
                Text("Today Only")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show currently selected currency
        Text(
            text = "Selected Currency: ${uiState.selectedCurrency}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Show error if there is one
        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Show loading indicator or data
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else if (uiState.divisas.isEmpty() && uiState.error == null) {
            Text(
                text = "No data available. Check logs to see query status.",
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            // Data list
            Text(
                text = "Found ${uiState.divisas.size} records:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn {
                items(uiState.divisas) { divisa ->
                    DivisaItem(divisa)
                }
            }
        }
    }
}

@Composable
fun DivisaItem(divisa: DivisaModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Currency: ${divisa.moneda}",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Rate: ${divisa.tasa}",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Date: ${divisa.fechaHora}")

            Spacer(modifier = Modifier.height(2.dp))

            // Calculate and show the inverted rate (1 MXN to currency)
            val invertedRate = 1.0 / divisa.tasa
            Text(
                text = "1 MXN = ${String.format("%.6f", invertedRate)} ${divisa.moneda}",
                fontWeight = FontWeight.Bold
            )
        }
    }

    Divider(modifier = Modifier.padding(vertical = 4.dp))
}