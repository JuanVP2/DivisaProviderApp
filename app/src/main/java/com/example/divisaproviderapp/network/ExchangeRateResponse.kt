package com.example.divisaproviderapp.network

import kotlinx.serialization.Serializable
import retrofit2.http.GET

@Serializable
data class ExchangeRateResponse(
    val result: String? = null,
    val provider: String? = null,
    val documentation: String? = null,
    val terms_of_use: String? = null,
    val time_last_update_unix: Long? = null,
    val time_last_update_utc: String? = null,
    val time_next_update_unix: Long? = null,
    val time_next_update_utc: String? = null,
    val time_eol_unix: Long? = null,
    val base_code: String,
    val conversion_rates: Map<String, Double>
)


interface DivisaApiService {
    @GET("latest/MXN")
    suspend fun getPrices(): ExchangeRateResponse
}
