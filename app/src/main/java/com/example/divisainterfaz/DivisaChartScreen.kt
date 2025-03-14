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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    label: String,
    dateTimeString: String,
    onDateTimeChange: (String) -> Unit
) {
    val context = LocalContext.current

    // Formato para mostrar al usuario
    val displayFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("America/Mexico_City")
    }

    // Formato para guardar internamente
    val internalFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // Calendar para manipular la fecha/hora actual
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
        // Muestra la fecha/hora en un TextField de solo lectura
        OutlinedTextField(
            value = displayFormat.format(calendar.time),
            onValueChange = { /* No editar manualmente */ },
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Botón para abrir DatePickerDialog y luego TimePickerDialog
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

/**
 * Pantalla principal que muestra:
 * - Selector de moneda
 * - Selector de fechas
 * - Botón para cargar datos
 * - Estadísticas
 * - Lista de divisas
 */
@OptIn(ExperimentalMaterial3Api::class)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
//                            Text(
//                                text = "Registros: ${listaDivisas.size}",
//                                modifier = Modifier.weight(1f)
//                            )

                            ultimaDivisa?.let { divisa ->
                                val inversa = 1.0 / divisa.tasa
                                Text(
                                    text = "1 MXN = ${String.format("%.6f", divisa.tasa)} ${divisa.moneda}",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

//                        primeraDivisa?.let { primera ->
//                            ultimaDivisa?.let { ultima ->
//                                val cambio = (1.0/ultima.tasa - 1.0/primera.tasa) / (1.0/primera.tasa) * 100
//                                Text(
//                                    text = "Variación: %.2f%%".format(cambio),
//                                    color = if (cambio >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
//                                )
//                            }
//                        }
                    }
                }
            }


            // Título para la lista
//            Text(
//                text = "Lista de Tasas (${listaDivisas.size} registros)",
//                style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier.padding(vertical = 8.dp)
//            )
            if (listaDivisas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp)) // Mayor espacio antes de la gráfica

                // Título específico para la gráfica
                Text(
                    text = "Gráfica de Evolución",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Gráfica de tipo de cambio con mayor prominencia
                ExchangeRateChart(
                    divisas = listaDivisas,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
            // Lista de tasas
//            LazyColumn {
//                items(listaDivisas.sortedByDescending { it.fechaHora }) { divisa ->
//                    DivisaItem(divisa)
//                }
//            }

        }
    }
}