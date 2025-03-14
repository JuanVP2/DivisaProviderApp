package com.example.divisainterfaz

import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Composable que muestra una gráfica lineal para los datos de tipo de cambio
 */
@Composable
fun ExchangeRateChart(divisas: List<DivisaModel>, modifier: Modifier = Modifier) {
    if (divisas.isEmpty()) {
        Log.d("ExchangeRateChart", "No hay datos para mostrar en la gráfica")
        return
    }

    Log.d("ExchangeRateChart", "Intentando mostrar gráfica con ${divisas.size} datos")

    val scrollState = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Evolución de Tipo de Cambio ${divisas.firstOrNull()?.moneda ?: ""}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Área de depuración - para verificar que llegamos a este punto
            Text(
                text = "Datos disponibles: ${divisas.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            val chartHeight = (divisas.size * 15).coerceAtLeast(300)

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight.dp),
                factory = { context ->
                    Log.d("ExchangeRateChart", "Creando instancia del LineChart")
                    LineChart(context).apply {
                        description.isEnabled = false
                        legend.isEnabled = true
                        setTouchEnabled(true)
                        setScaleEnabled(true)
                        setPinchZoom(true)

                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.labelRotationAngle = -45f

                        axisRight.isEnabled = false
                        axisLeft.setDrawGridLines(false)

                        animateX(1500)
                    }
                },
                update = { chart ->
                    Log.d("ExchangeRateChart", "Actualizando datos del chart")
                    updateChartWithData(chart, divisas)
                }
            )

            // Añadimos un espacio al final para mejorar la visualización con scroll
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Función auxiliar para actualizar la gráfica con datos de tipo de cambio
 */
private fun updateChartWithData(chart: LineChart, divisas: List<DivisaModel>) {
    try {
        // Ordenar datos por fecha
        val sortedDivisas = divisas.sortedBy { parseDate(it.fechaHora) }

        Log.d("ExchangeRateChart", "Datos ordenados: ${sortedDivisas.size}")

        if (sortedDivisas.isEmpty()) {
            Log.e("ExchangeRateChart", "No hay datos para mostrar después de ordenar")
            return
        }

        // Crear entradas para la gráfica
        val entries = sortedDivisas.mapIndexed { index, divisa ->
            val value = divisa.tasa.toFloat()
            Entry(index.toFloat(), value)
        }

        //  dataset a partir de las entradas
        val dataSet = LineDataSet(entries, "${sortedDivisas.first().moneda} por 1 MXN").apply {
            color = Color.BLUE
            lineWidth = 3f
            circleRadius = 4f
            setCircleColor(Color.BLUE)
            setDrawValues(false) // Ocultar valores
            valueTextSize = 9f
            mode = LineDataSet.Mode.LINEAR
            setDrawFilled(true)
            fillColor = Color.rgb(65, 105, 225)
            fillAlpha = 40
        }

        // Establecer datos en la gráfica
        chart.data = LineData(dataSet)

        // Crear formateador personalizado para el eje X para mostrar fechas
        val formatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val position = value.toInt()
                if (position >= 0 && position < sortedDivisas.size) {
                    // Formatear la fecha para mostrar
                    return formatDateForDisplay(sortedDivisas[position].fechaHora)
                }
                return ""
            }
        }

        chart.xAxis.valueFormatter = formatter

        // Número óptimo de etiquetas basado en la cantidad de datos
        val labelCount = when {
            sortedDivisas.size <= 7 -> sortedDivisas.size
            sortedDivisas.size <= 30 -> 7
            else -> 10
        }
        chart.xAxis.labelCount = labelCount

        // Ajustamos la visibilidad para mostrar todos los datos
        chart.setVisibleXRangeMaximum(10f) // Mostrar máximo 10 puntos a la vez para mejor visualización

        // Refrescar la gráfica
        chart.invalidate()

        Log.d("ExchangeRateChart", "Gráfica actualizada con ${entries.size} puntos de datos")
    } catch (e: Exception) {
        Log.e("ExchangeRateChart", "Error al actualizar la gráfica", e)
    }
}

/**
 * Función auxiliar para analizar cadena de fecha a objeto Date
 */
private fun parseDate(dateString: String): Date {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        formatter.parse(dateString) ?: Date()
    } catch (e: Exception) {
        Log.e("ExchangeRateChart", "Error parseando fecha: $dateString", e)
        Date()
    }
}

/**
 * Función auxiliar para formatear cadena de fecha para mostrar en la gráfica
 */
private fun formatDateForDisplay(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateString) ?: return dateString

        val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        Log.e("ExchangeRateChart", "Error formateando fecha: $dateString", e)
        dateString
    }
}