package com.example.divisainterfaz

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DivisaChartViewModel(private val repository: DivisaClientRepository) : ViewModel() {

    // State for chart data
    private val _divisasPorRango = MutableStateFlow<List<DivisaModel>>(emptyList())
    val divisasPorRango: StateFlow<List<DivisaModel>> = _divisasPorRango.asStateFlow()

    // UI State for loading and errors
    private val _chartUiState = MutableStateFlow(ChartUiState())
    val chartUiState: StateFlow<ChartUiState> = _chartUiState.asStateFlow()

    init {
        Log.d("DivisaChartViewModel", "ViewModel initialized")
        // Initialize with default dates
        val endDate = getCurrentFormattedDate()
        val startDate = getDateBefore(60) // Last 60 days by default
        cargarDivisasPorRango("USD", startDate, endDate)
    }

    // Load data for a specific date range
    fun cargarDivisasPorRango(moneda: String, fechaInicio: String, fechaFin: String) {
        viewModelScope.launch {
            Log.d("DivisaChartViewModel", "Loading rates for $moneda from $fechaInicio to $fechaFin")
            _chartUiState.update { it.copy(isLoading = true, error = null) }

            try {
                val rates = repository.getDivisasByRange(moneda, fechaInicio, fechaFin)
                Log.d("DivisaChartViewModel", "Loaded ${rates.size} rates")

                _divisasPorRango.value = rates
                _chartUiState.update {
                    it.copy(
                        isLoading = false,
                        selectedCurrency = moneda
                    )
                }

                if (rates.isEmpty()) {
                    Log.w("DivisaChartViewModel", "No rates found for this range")
                    _chartUiState.update { it.copy(error = "No data available for this date range") }
                }
            } catch (e: Exception) {
                Log.e("DivisaChartViewModel", "Error loading rates", e)
                _chartUiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load rates: ${e.message}"
                    )
                }
            }
        }
    }

    // Helper method to get current date in required format
    private fun getCurrentFormattedDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    // Helper method to get a date X days before current date
    private fun getDateBefore(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    // Format date from one format to another
    fun formatDate(dateStr: String, inputFormat: String, outputFormat: String): String {
        return try {
            val inputSdf = SimpleDateFormat(inputFormat, Locale.getDefault())
            val outputSdf = SimpleDateFormat(outputFormat, Locale.getDefault())
            val date = inputSdf.parse(dateStr)
            if (date != null) {
                outputSdf.format(date)
            } else {
                dateStr
            }
        } catch (e: Exception) {
            Log.e("DivisaChartViewModel", "Error formatting date: $dateStr", e)
            dateStr
        }
    }

    // Factory to create ViewModel with repository
    class Factory(private val repository: DivisaClientRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DivisaChartViewModel::class.java)) {
                return DivisaChartViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// UI State for the chart screen
data class ChartUiState(
    val isLoading: Boolean = false,
    val selectedCurrency: String = "USD",
    val error: String? = null
)