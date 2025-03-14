package com.example.divisaproviderapp.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import com.example.divisaproviderapp.data.DivisaDatabase
import com.example.divisaproviderapp.model.Divisa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class DivisaContentProvider : ContentProvider() {

    companion object {
        // Ajusta la authority a tu gusto
        const val AUTHORITY = "com.example.divisaproviderapp.provider"
        const val TABLE_NAME = "divisas"

        const val COLUMN_ID = "id"
        const val COLUMN_MONEDA = "moneda"
        const val COLUMN_TASA = "tasa"
        const val COLUMN_FECHAHORA = "fechaHora"

        // Paquete de la aplicación permitida para acceder
        private const val ALLOWED_PACKAGE = "com.example.divisainterfaz"
    }

    private lateinit var database: DivisaDatabase

    override fun onCreate(): Boolean {
        context?.let { ctx ->
            database = DivisaDatabase.getInstance(ctx)

            runBlocking {
                val lista = withContext(Dispatchers.IO) {
                    database.divisaDao().obtenerDivisasPorRango(
                        "USD",
                        "0000-01-01 00:00:00",
                        "9999-12-31 23:59:59"
                    )
                }

                Log.d("DivisaContentProvider", "Al iniciar, hay ${lista.size} registros de USD en la DB.")

                // Si no hay datos, insertar datos de prueba
                if (lista.isEmpty()) {
                    insertarDatosEjemplo()
                }
            }
        }
        return true
    }

    /**
     * Inserta datos de ejemplo si la base de datos está vacía.
     */
    private suspend fun insertarDatosEjemplo() {
        val dao = database.divisaDao()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val ahora = System.currentTimeMillis()

        val listaEjemplo = listOf(
            Divisa(moneda = "USD", tasa = 18.50, fechaHora = sdf.format(ahora - 3600000L)),
            Divisa(moneda = "USD", tasa = 18.60, fechaHora = sdf.format(ahora)),
            Divisa(moneda = "USD", tasa = 18.55, fechaHora = sdf.format(ahora + 3600000L)),
            Divisa(moneda = "EUR", tasa = 19.90, fechaHora = sdf.format(ahora))
        )

        dao.insertarDivisas(listaEjemplo)

        // Log para verificar cuántos registros hay ahora
        val divisasTrasInsert = dao.obtenerDivisasPorRango(
            currency = "USD",
            startDate = "0000-01-01 00:00:00",
            endDate = "9999-12-31 23:59:59"
        )
        Log.d("DivisaContentProvider", "Se insertaron datos de ejemplo. Ahora hay ${divisasTrasInsert.size} registros USD en la DB.")
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        // 🚨 Verifica que solo la app permitida pueda acceder
        val callingPackage = callingPackage ?: return null
        if (callingPackage != ALLOWED_PACKAGE) {
            Log.e("DivisaContentProvider", "Acceso denegado a: $callingPackage")
            return null
        }

        // Manejar endpoint para obtener monedas disponibles
        val uriPath = uri.path
        if (uriPath?.endsWith("/currencies") == true) {
            return getAvailableCurrencies()
        }

        // 🔹 Extraer parámetros de la URI para búsqueda normal
        val currency = uri.getQueryParameter("currency") ?: return null
        val startDate = uri.getQueryParameter("startDate") ?: return null
        val endDate = uri.getQueryParameter("endDate") ?: return null

        // ❌ Validar parámetros antes de hacer la consulta
        if (currency.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Log.e("DivisaContentProvider", "Parámetros inválidos en la consulta.")
            return null
        }

        // 🔹 Ejecutar consulta en un hilo de I/O
        val listaDivisas = runBlocking(Dispatchers.IO) {
            database.divisaDao().obtenerDivisasPorRango(currency, startDate, endDate)
        }

        // 🔹 Convertir lista a Cursor
        val cursor = MatrixCursor(arrayOf(COLUMN_ID, COLUMN_MONEDA, COLUMN_TASA, COLUMN_FECHAHORA))
        listaDivisas.forEach { divisa ->
            cursor.addRow(arrayOf(divisa.id, divisa.moneda, divisa.tasa, divisa.fechaHora))
        }

        return cursor
    }

    /**
     * Obtiene la lista de monedas disponibles en la base de datos
     */
    private fun getAvailableCurrencies(): Cursor {
        // Crear cursor para las monedas
        val cursor = MatrixCursor(arrayOf("currency"))

        // Obtener monedas únicas de la base de datos
        val currencies = runBlocking(Dispatchers.IO) {
            database.divisaDao().obtenerMonedasDisponibles()
        }

        Log.d("DivisaContentProvider", "Monedas disponibles: $currencies")

        // Agregar cada moneda al cursor
        currencies.forEach { currency ->
            cursor.addRow(arrayOf(currency))
        }

        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun getType(uri: Uri): String? = null
}