package com.example.divisaproviderapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.divisaproviderapp.model.Divisa

@Dao
interface DivisaDao {

    // Insert con IGNORE para no sobrescribir si hay conflicto en la PK
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarDivisas(divisas: List<Divisa>)

    // Por si quieres filtrar por fecha exacta
    @Query("SELECT * FROM divisas WHERE fechaHora LIKE :fecha || '%'")
    suspend fun obtenerDivisasPorFecha(fecha: String): List<Divisa>

    // Consulta por rango de fecha y moneda
    @Query("""
        SELECT * FROM divisas
        WHERE moneda = :currency
          AND fechaHora BETWEEN :startDate AND :endDate
        ORDER BY fechaHora ASC
    """)
    suspend fun obtenerDivisasPorRango(
        currency: String,
        startDate: String,
        endDate: String
    ): List<Divisa>
}
