package com.example.divisaproviderapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.divisaproviderapp.model.Divisa

@Dao
interface DivisaDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarDivisas(divisas: List<Divisa>)

    @Query("SELECT DISTINCT moneda FROM divisas ORDER BY moneda ASC")
    suspend fun obtenerMonedasDisponibles(): List<String>

    @Query("SELECT * FROM divisas WHERE moneda = :currency AND fechaHora BETWEEN :startDate AND :endDate ORDER BY fechaHora")
    suspend fun obtenerDivisasPorRango(currency: String, startDate: String, endDate: String): List<Divisa>
}
