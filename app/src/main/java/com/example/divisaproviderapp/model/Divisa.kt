package com.example.divisaproviderapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "divisas")
data class Divisa(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val moneda: String,
    val tasa: Double,
    val fechaHora: String
)
