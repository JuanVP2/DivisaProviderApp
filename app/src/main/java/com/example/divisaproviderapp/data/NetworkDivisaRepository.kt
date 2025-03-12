package com.example.divisaproviderapp.data

import android.util.Log
import com.example.divisaproviderapp.model.Divisa
import com.example.divisaproviderapp.network.DivisaApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class NetworkDivisaRepository(
    private val apiService: DivisaApiService,
    private val dao: DivisaDao
) : DivisaRepository {

    override suspend fun sincronizarDivisas() {
        try {
            val respuesta = apiService.getPrices()
            val fechaActual = obtenerFechaActual()

            val listaDivisas = respuesta.conversion_rates.map { (moneda, tasa) ->
                Divisa(moneda = moneda, tasa = tasa, fechaHora = fechaActual)
            }

            withContext(Dispatchers.IO) {
                dao.insertarDivisas(listaDivisas)
            }

            Log.d("NetworkDivisaRepository", "Sincronización exitosa en $fechaActual")
        } catch (e: Exception) {
            Log.e("NetworkDivisaRepository", "Error al sincronizar divisas:", e)
        }
    }

    override suspend fun obtenerDivisasPorRango(
        currency: String,
        startDate: String,
        endDate: String
    ): List<Divisa> {
        return withContext(Dispatchers.IO) {
            dao.obtenerDivisasPorRango(currency, startDate, endDate)
        }
    }

    private fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("America/Mexico_City")
        }
        return sdf.format(java.util.Date())
    }
}
