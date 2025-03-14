package com.example.divisainterfaz


import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.divisainterfaz.DivisaModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DivisaClientRepository(private val context: Context) {

    companion object {
        private const val AUTHORITY = "com.example.divisaproviderapp.provider"
        private const val BASE_PATH = "divisas"
        private val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$BASE_PATH")


        private const val COLUMN_ID = "id"
        private const val COLUMN_MONEDA = "moneda"
        private const val COLUMN_TASA = "tasa"
        private const val COLUMN_FECHAHORA = "fechaHora"
    }

    // Base URI provider
    private val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$BASE_PATH")


    suspend fun getDivisasByRange(
        currency: String,
        startDate: String,
        endDate: String
    ): List<DivisaModel> = withContext(Dispatchers.IO) {
        val result = mutableListOf<DivisaModel>()

        try {
            Log.d("DivisaClientRepo", "Attempting to query provider for $currency from $startDate to $endDate")

            val uri = Uri.parse("content://$AUTHORITY/$BASE_PATH")
                .buildUpon()
                .appendQueryParameter("currency", currency)
                .appendQueryParameter("startDate", startDate)
                .appendQueryParameter("endDate", endDate)
                .build()
            Log.d("DivisaClientRepo", "Querying URI: $uri")
            Log.d("DivisaClientRepo", "Query params: currency=$currency, startDate=$startDate, endDate=$endDate")
            // Query  content provider
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                Log.d("DivisaClientRepo", "Query successful! Found ${cursor.count} records")

                if (cursor.count > 0) {
                    //   indices
                    val idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID)
                    val monedaIndex = cursor.getColumnIndexOrThrow(COLUMN_MONEDA)
                    val tasaIndex = cursor.getColumnIndexOrThrow(COLUMN_TASA)
                    val fechaHoraIndex = cursor.getColumnIndexOrThrow(COLUMN_FECHAHORA)

                    // todos rows
                    while (cursor.moveToNext()) {
                        val divisa = DivisaModel(
                            id = cursor.getLong(idIndex),
                            moneda = cursor.getString(monedaIndex),
                            tasa = cursor.getDouble(tasaIndex),
                            fechaHora = cursor.getString(fechaHoraIndex)
                        )
                        result.add(divisa)
                        Log.d("DivisaClientRepo", "Retrieved: $divisa")
                    }
                } else {
                    Log.w("DivisaClientRepo", "No data returned from provider")
                }
            } ?: run {
                Log.e("DivisaClientRepo", "Failed to query content provider - null cursor returned")
            }
        } catch (e: Exception) {
            Log.e("DivisaClientRepo", "Error querying content provider", e)
        }

        return@withContext result
    }
    /**
     * Gets available currencies from the provider
     */
    suspend fun getAvailableCurrencies(): List<String> = withContext(Dispatchers.IO) {
        val currencies = mutableListOf<String>()

        try {
            val uri = Uri.parse("content://$AUTHORITY/$BASE_PATH/currencies")
            Log.d("DivisaClientRepo", "Querying available currencies: $uri")

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                Log.d("DivisaClientRepo", "Found ${cursor.count} currencies")

                val currencyColumnIndex = cursor.getColumnIndexOrThrow("currency")
                while (cursor.moveToNext()) {
                    val currency = cursor.getString(currencyColumnIndex)
                    currencies.add(currency)
                }
            } ?: run {
                currencies.addAll(listOf("USD", "EUR", "GBP", "JPY", "CAD"))

            }
        } catch (e: Exception) {
            Log.e("DivisaClientRepo", "Error querying currencies", e)
            currencies.addAll(listOf("USD", "EUR", "GBP", "JPY", "CAD"))
        }

        return@withContext currencies
    }


}