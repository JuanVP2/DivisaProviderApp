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
        // Authority must match the ContentProvider's authority
        private const val AUTHORITY = "com.example.divisaproviderapp.provider"
        private const val BASE_PATH = "divisas"
        private val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$BASE_PATH")


        // Column names in the provider
        private const val COLUMN_ID = "id"
        private const val COLUMN_MONEDA = "moneda"
        private const val COLUMN_TASA = "tasa"
        private const val COLUMN_FECHAHORA = "fechaHora"
    }

    // Base URI for the content provider
    private val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$BASE_PATH")


    /**
     * Gets exchange rates for the specified currency and date range
     */
    suspend fun getDivisasByRange(
        currency: String,
        startDate: String,
        endDate: String
    ): List<DivisaModel> = withContext(Dispatchers.IO) {
        val result = mutableListOf<DivisaModel>()

        try {
            Log.d("DivisaClientRepo", "Attempting to query provider for $currency from $startDate to $endDate")

            // Build URI with query parameters
            val uri = Uri.parse("content://$AUTHORITY/$BASE_PATH")
                .buildUpon()
                .appendQueryParameter("currency", currency)
                .appendQueryParameter("startDate", startDate)
                .appendQueryParameter("endDate", endDate)
                .build()
            Log.d("DivisaClientRepo", "Querying URI: $uri")
            // Query the content provider
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                Log.d("DivisaClientRepo", "Query successful! Found ${cursor.count} records")

                if (cursor.count > 0) {
                    // Get column indices
                    val idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID)
                    val monedaIndex = cursor.getColumnIndexOrThrow(COLUMN_MONEDA)
                    val tasaIndex = cursor.getColumnIndexOrThrow(COLUMN_TASA)
                    val fechaHoraIndex = cursor.getColumnIndexOrThrow(COLUMN_FECHAHORA)

                    // Iterate through all rows
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
     * Gets exchange rates for a specific currency for the last 7 days
     */
    suspend fun getLast7DaysRates(currency: String): List<DivisaModel> {
        val endDate = getCurrentFormattedDate()
        val startDate = getDateBefore(7)

        Log.d("DivisaClientRepo", "Fetching last 7 days of $currency from $startDate to $endDate")
        return getDivisasByRange(currency, startDate, endDate)
    }

    /**
     * Gets today's exchange rates for a specific currency
     */
    suspend fun getTodayRates(currency: String): List<DivisaModel> {
        val today = getCurrentFormattedDate().split(" ")[0] // Just the date part
        val startDate = "$today 00:00:00"
        val endDate = "$today 23:59:59"

        Log.d("DivisaClientRepo", "Fetching today's $currency rates from $startDate to $endDate")
        return getDivisasByRange(currency, startDate, endDate)
    }

    /**
     * Gets the current formatted date
     */
    private fun getCurrentFormattedDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Gets a date X days before current date, formatted properly
     */
    private fun getDateBefore(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(calendar.time)
    }
}