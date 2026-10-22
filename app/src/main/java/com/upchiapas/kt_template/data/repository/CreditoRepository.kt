package com.upchiapas.kt_template.data.repository

import com.upchiapas.kt_template.model.CreditoEntity
import kotlinx.coroutines.flow.Flow

interface CreditoRepository {
    suspend fun insertarCredito(credito: CreditoEntity)
    fun obtenerHistorial(): Flow<List<CreditoEntity>>
    suspend fun borrarTodo()
}
