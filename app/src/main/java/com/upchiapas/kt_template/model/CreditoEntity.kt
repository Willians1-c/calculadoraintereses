package com.upchiapas.kt_template.model



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historial_creditos")
data class CreditoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val monto: Double,
    val tasa: Double,
    val aporteExtra: Double,
    val plazoMeses: Int,
    val esInteresFijo: Boolean,
    val cuotaMensual: Double,
    val totalIntereses: Double,
    val totalPagar: Double,
    val fechaGuardado: Long = System.currentTimeMillis() // cuándo lo guarde
)