package com.upchiapas.kt_template.data.repository

import com.upchiapas.kt_template.data.CreditoDao
import com.upchiapas.kt_template.model.CreditoEntity
import kotlinx.coroutines.flow.Flow

class CreditoRepositoryImpl(
    private val creditoDao: CreditoDao
) : CreditoRepository {
    override suspend fun insertarCredito(credito: CreditoEntity) {
        creditoDao.insertarCredito(credito)
    }

    override fun obtenerHistorial(): Flow<List<CreditoEntity>> {
        return creditoDao.obtenerHistorial()
    }

    override suspend fun borrarTodo() {
        creditoDao.borrarTodo()
    }
}
