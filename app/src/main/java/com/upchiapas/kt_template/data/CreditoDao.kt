package com.upchiapas.kt_template.data


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.upchiapas.kt_template.model.CreditoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditoDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCredito(credito: CreditoEntity)

    @Query("SELECT * FROM historial_creditos ORDER BY fechaGuardado DESC")
    fun obtenerHistorial(): Flow<List<CreditoEntity>>


    @Query("DELETE FROM historial_creditos")
    suspend fun borrarTodo()
}