// 1. Verificar que el archivo ExchangeRateChart.kt está completo
// Asegúrate de que este archivo esté en el mismo paquete que el resto de tu código

package com.example.divisainterfaz

import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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

            AndroidView(
                modifier = Modifier.size(width = 400.dp, height = 10000.dp),
                factory = { context ->
                    Log.d("ExchangeRateChart", "Creando instancia del LineChart")
                    LineChart(context).apply {
                        description.isEnabled = false
                        legend.isEnabled = true
                        setTouchEnabled(true)
                        setScaleEnabled(true)
                        setPinchZoom(true)

                        // Configurar eje X (fechas)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.labelRotationAngle = -45f

                        // Configurar eje Y (tasas de cambio)
                        axisRight.isEnabled = false  // Deshabilitar eje Y derecho
                        axisLeft.setDrawGridLines(false)

                        // Animación
                        animateX(1500)
                    }
                },
                update = { chart ->
                    Log.d("ExchangeRateChart", "Actualizando datos del chart")
                    updateChartWithData(chart, divisas)
                }
            )
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
            // Usar el valor de tasa invertido para representar moneda por 1 MXN
            val value = divisa.tasa.toFloat()
            Entry(index.toFloat(), value)
            Log.d("ExchangeRateChart", "Punto $index: fecha=${divisa.fechaHora}, valor=$value")
            Entry(index.toFloat(), value)
        }

        // Crear dataset a partir de las entradas
        val dataSet = LineDataSet(entries, "${sortedDivisas.first().moneda} por 1 MXN").apply {
            color = Color.BLUE
            lineWidth = 3f
            circleRadius = 4f
            setCircleColor(Color.BLUE)
            setDrawValues(true) // Mostrar valores para depuración
            valueTextSize = 9f
            mode = LineDataSet.Mode.LINEAR  // Modo lineal para depuración
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

        // Número óptimo de etiquetas
        val labelCount = minOf(5, sortedDivisas.size)
        chart.xAxis.labelCount = labelCount

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