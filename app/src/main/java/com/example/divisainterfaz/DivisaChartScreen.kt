package com.example.divisainterfaz

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Componente para mostrar un elemento de divisa
 */
@Composable
fun DivisaItem(divisa: DivisaModel) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = divisa.moneda,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "1 MXN = ${String.format("%.6f", 1.0/divisa.tasa)} ${divisa.moneda}",
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = "Fecha: ${divisa.fechaHora}",
            style = MaterialTheme.typography.bodySmall
        )

    }
}

/**
 * Composable para seleccionar fecha/hora.
 */
@Composable
fun DateTimePickerField(
    label: String,
    dateTimeString: String,
    onDateTimeChange: (String) -> Unit
) {
    val context = LocalContext.current

    val displayFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("America/Mexico_City")
    }

    val internalFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    val calendar = Calendar.getInstance().apply {
        timeZone = TimeZone.getTimeZone("America/Mexico_City")
        try {
            // Intentamos parsear la fecha en formato interno primero
            val parsedDate = runCatching { internalFormat.parse(dateTimeString) }
                .getOrNull() ?: displayFormat.parse(dateTimeString)

            if (parsedDate != null) {
                time = parsedDate
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    Column {
        OutlinedTextField(
            value = displayFormat.format(calendar.time),
            onValueChange = { /* No editar manualmente */ },
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(onClick = {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    // Luego abrimos TimePicker
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            calendar.set(Calendar.MINUTE, minute)
                            calendar.set(Calendar.SECOND, 0)

                            val newDateTime = internalFormat.format(calendar.time)
                            onDateTimeChange(newDateTime)
                            Log.d("DateTimePickerField", "Nueva fecha/hora: $newDateTime")
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }) {
            Text("Seleccionar $label")
        }
    }
}

@Composable
fun DivisaChartScreen(viewModel: DivisaChartViewModel) {
    // Estados de UI
    val chartUiState by viewModel.chartUiState.collectAsState()
    val listaDivisas by viewModel.divisasPorRango.collectAsState()
    val availableCurrencies by viewModel.availableCurrencies.collectAsState()

    // Estado para la moneda
    var moneda by remember { mutableStateOf(chartUiState.selectedCurrency) }

    // Estados para "Desde" y "Hasta" en formato "yyyy-MM-dd HH:mm:ss"
    val defaultStartDate = remember {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -2) // 2 meses atrás
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(calendar.time)
    }

    val defaultEndDate = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    var fechaInicio by remember { mutableStateOf(defaultStartDate) }
    var fechaFin by remember { mutableStateOf(defaultEndDate) }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Visualizador de Divisas",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Campo de texto para la moneda
            CurrencyDropdown(
                selectedCurrency = moneda,
                availableCurrencies = availableCurrencies,
                onCurrencySelected = { newCurrency ->
                    moneda = newCurrency
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo "Desde"
            DateTimePickerField(
                label = "Desde",
                dateTimeString = fechaInicio,
                onDateTimeChange = { nuevaFecha ->
                    fechaInicio = nuevaFecha
                    Log.d("DivisaChartScreen", "Fecha inicio -> $nuevaFecha")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo "Hasta"
            DateTimePickerField(
                label = "Hasta",
                dateTimeString = fechaFin,
                onDateTimeChange = { nuevaFecha ->
                    fechaFin = nuevaFecha
                    Log.d("DivisaChartScreen", "Fecha fin -> $nuevaFecha")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para cargar datos
            Button(
                onClick = {
                    viewModel.cargarDivisasPorRango(moneda, fechaInicio, fechaFin)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cargar Datos")
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Mostrar error si hay
            chartUiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Mostrar contenido o indicador de carga
            if (chartUiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (listaDivisas.isEmpty() && chartUiState.error == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "No hay datos disponibles para mostrar",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                if (listaDivisas.isNotEmpty()) {
                    val ultimaDivisa = listaDivisas.maxByOrNull { it.fechaHora }
                    val primeraDivisa = listaDivisas.minByOrNull { it.fechaHora }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Estadísticas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Row(modifier = Modifier.fillMaxWidth()) {
                                ultimaDivisa?.let { divisa ->
                                    val inversa = 1.0 / divisa.tasa
                                    Text(
                                        text = "1 MXN = ${String.format("%.6f", inversa)} ${divisa.moneda}",
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                if (listaDivisas.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Gráfica de Evolución",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    ) {
                        ExchangeRateChart(
                            divisas = listaDivisas,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}