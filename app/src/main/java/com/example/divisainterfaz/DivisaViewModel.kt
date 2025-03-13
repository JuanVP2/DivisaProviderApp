package com.example.divisainterfaz


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.divisainterfaz.DivisaModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DivisaViewModel(private val repository: DivisaClientRepository) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(DivisaUiState())
    val uiState: StateFlow<DivisaUiState> = _uiState.asStateFlow()

    init {
        Log.d("DivisaViewModel", "ViewModel initialized")
        loadUsdRates()
    }

    // Load USD rates for the last 7 days
    fun loadUsdRates() {
        viewModelScope.launch {
            Log.d("DivisaViewModel", "Loading USD rates")
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val usdRates = repository.getLast7DaysRates("USD")
                Log.d("DivisaViewModel", "Loaded ${usdRates.size} USD rates")

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        divisas = usdRates,
                        selectedCurrency = "USD"
                    )
                }

                if (usdRates.isEmpty()) {
                    Log.w("DivisaViewModel", "No USD rates found")
                    _uiState.update { it.copy(error = "No data available for USD") }
                }
            } catch (e: Exception) {
                Log.e("DivisaViewModel", "Error loading USD rates", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load rates: ${e.message}"
                    )
                }
            }
        }
    }

    // Load EUR rates for the last 7 days
    fun loadEurRates() {
        viewModelScope.launch {
            Log.d("DivisaViewModel", "Loading EUR rates")
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val eurRates = repository.getLast7DaysRates("EUR")
                Log.d("DivisaViewModel", "Loaded ${eurRates.size} EUR rates")

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        divisas = eurRates,
                        selectedCurrency = "EUR"
                    )
                }

                if (eurRates.isEmpty()) {
                    Log.w("DivisaViewModel", "No EUR rates found")
                    _uiState.update { it.copy(error = "No data available for EUR") }
                }
            } catch (e: Exception) {
                Log.e("DivisaViewModel", "Error loading EUR rates", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load rates: ${e.message}"
                    )
                }
            }
        }
    }

    // Load today's rates for a specific currency
    fun loadTodayRates(currency: String) {
        viewModelScope.launch {
            Log.d("DivisaViewModel", "Loading today's $currency rates")
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val todayRates = repository.getTodayRates(currency)
                Log.d("DivisaViewModel", "Loaded ${todayRates.size} rates for today")

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        divisas = todayRates,
                        selectedCurrency = currency
                    )
                }

                if (todayRates.isEmpty()) {
                    Log.w("DivisaViewModel", "No rates found for today")
                    _uiState.update { it.copy(error = "No data available for today") }
                }
            } catch (e: Exception) {
                Log.e("DivisaViewModel", "Error loading today's rates", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load today's rates: ${e.message}"
                    )
                }
            }
        }
    }

    // Factory to create ViewModel with repository
    class Factory(private val repository: DivisaClientRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DivisaViewModel::class.java)) {
                return DivisaViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// UI State for the app
data class DivisaUiState(
    val isLoading: Boolean = false,
    val divisas: List<DivisaModel> = emptyList(),
    val selectedCurrency: String = "USD",
    val error: String? = null
)