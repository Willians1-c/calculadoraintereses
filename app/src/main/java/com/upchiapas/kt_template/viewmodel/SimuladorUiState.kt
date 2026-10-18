package com.upchiapas.kt_template.viewmodel

import com.upchiapas.kt_template.model.PagoMensual

data class SimuladorUiState(
    val monto: String = "100000",
    val tasa: String = "12.5",
    val aporteExtra: String = "500",
    val plazoMeses: Float = 24f,
    val esInteresFijo: Boolean = true,
    val cuotaMensual: Double = 0.0,
    val cuotaTotalConAporte: Double = 0.0,
    val totalIntereses: Double = 0.0,
    val totalPagar: Double = 0.0,
    val mesesLiquidados: Int = 0,
    val ahorroIntereses: Double = 0.0,
    val tablaAmortizacion: List<PagoMensual> = emptyList()
)
