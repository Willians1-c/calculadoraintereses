package com.upchiapas.kt_template.model

data class PagoMensual(
    val mes: Int,
    val cuota: Double,
    val capital: Double,
    val interes: Double,
    val saldo: Double
)