package com.example.divisaproviderapp.data

import com.example.divisaproviderapp.model.Divisa

interface DivisaRepository {
    suspend fun sincronizarDivisas()
    suspend fun obtenerDivisasPorRango(currency: String, startDate: String, endDate: String): List<Divisa>
}