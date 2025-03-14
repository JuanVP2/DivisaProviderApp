package com.example.divisainterfaz

import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.layout.*
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
import java.util.*

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
            .fillMaxHeight(0.6f)
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Evolución de Tipo de Cambio ${divisas.firstOrNull()?.moneda ?: ""}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Datos disponibles: ${divisas.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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

                        setExtraOffsets(10f, 10f, 10f, 20f)

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

private fun updateChartWithData(chart: LineChart, divisas: List<DivisaModel>) {
    try {
        val sortedDivisas = divisas.sortedBy { parseDate(it.fechaHora) }

        if (sortedDivisas.isEmpty()) {
            Log.e("ExchangeRateChart", "No hay datos para mostrar después de ordenar")
            return
        }

        val entries = sortedDivisas.mapIndexed { index, divisa ->
            Entry(index.toFloat(), divisa.tasa.toFloat())
        }

        val dataSet = LineDataSet(entries, "${sortedDivisas.first().moneda} por 1 MXN").apply {
            color = Color.BLUE
            lineWidth = 3f
            circleRadius = 4f
            setCircleColor(Color.BLUE)
            setDrawValues(false)
            valueTextSize = 9f
            mode = LineDataSet.Mode.LINEAR
            setDrawFilled(true)
            fillColor = Color.rgb(65, 105, 225)
            fillAlpha = 40
        }

        chart.data = LineData(dataSet)

        val formatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val position = value.toInt()
                return if (position in sortedDivisas.indices) {
                    formatDateForDisplay(sortedDivisas[position].fechaHora)
                } else ""
            }
        }

        chart.xAxis.valueFormatter = formatter

        val labelCount = when {
            sortedDivisas.size <= 7 -> sortedDivisas.size
            sortedDivisas.size <= 30 -> 7
            else -> 10
        }
        chart.xAxis.labelCount = labelCount

        chart.setVisibleXRangeMaximum((divisas.size / 2).coerceAtLeast(5).toFloat())

        chart.layoutParams.width = chart.width
        chart.layoutParams.height = chart.height

        chart.invalidate()

        Log.d("ExchangeRateChart", "Gráfica actualizada con ${entries.size} puntos de datos")
    } catch (e: Exception) {
        Log.e("ExchangeRateChart", "Error al actualizar la gráfica", e)
    }
}

private fun parseDate(dateString: String): Date {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        formatter.parse(dateString) ?: Date()
    } catch (e: Exception) {
        Log.e("ExchangeRateChart", "Error parseando fecha: $dateString", e)
        Date()
    }
}

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