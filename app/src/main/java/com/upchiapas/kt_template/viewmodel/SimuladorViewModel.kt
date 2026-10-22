package com.upchiapas.kt_template.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.upchiapas.kt_template.data.repository.CreditoRepository
import com.upchiapas.kt_template.model.CreditoEntity
import com.upchiapas.kt_template.model.PagoMensual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.pow

class SimuladorViewModel(private val repository: CreditoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SimuladorUiState())
    val uiState: StateFlow<SimuladorUiState> = _uiState.asStateFlow()

    val historialCreditos: StateFlow<List<CreditoEntity>> = repository.obtenerHistorial()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        calcularAmortizacion()
    }

    fun guardarSimulacion() {
        val estado = _uiState.value

        if (estado.cuotaMensual <= 0.0) return

        viewModelScope.launch {
            val nuevoRegistro = CreditoEntity(
                monto = estado.monto.aDoubleSeguro(),
                tasa = estado.tasa.aDoubleSeguro(),
                aporteExtra = estado.aporteExtra.aDoubleSeguro(),
                plazoMeses = estado.plazoMeses.toInt(),
                esInteresFijo = estado.esInteresFijo,
                cuotaMensual = estado.cuotaMensual,
                totalIntereses = estado.totalIntereses,
                totalPagar = estado.totalPagar
            )
            repository.insertarCredito(nuevoRegistro)
        }
    }

    fun borrarHistorial() {
        viewModelScope.launch {
            repository.borrarTodo()
        }
    }

    private fun String.aDoubleSeguro(): Double {
        if (this.isBlank()) return 0.0
        val textoLimpio = this.replace(",", ".").trim()
        return textoLimpio.toDoubleOrNull() ?: 0.0
    }

    fun actualizarMonto(nuevoMonto: String) {
        _uiState.update { it.copy(monto = nuevoMonto) }
        calcularAmortizacion()
    }

    fun actualizarTasa(nuevaTasa: String) {
        _uiState.update { it.copy(tasa = nuevaTasa) }
        calcularAmortizacion()
    }

    fun actualizarAporteExtra(nuevoAporte: String) {
        _uiState.update { it.copy(aporteExtra = nuevoAporte) }
        calcularAmortizacion()
    }

    fun actualizarPlazo(nuevosMeses: Float) {
        _uiState.update { it.copy(plazoMeses = nuevosMeses) }
        calcularAmortizacion()
    }

    fun alternarTipoInteres(esFijo: Boolean) {
        _uiState.update { it.copy(esInteresFijo = esFijo) }
        calcularAmortizacion()
    }

    fun calcularAmortizacion() {
        val estadoActual = _uiState.value

        val capital = estadoActual.monto.aDoubleSeguro()
        val tasaAnual = estadoActual.tasa.aDoubleSeguro()
        val meses = estadoActual.plazoMeses.toInt()
        val aporteExtraMensual = estadoActual.aporteExtra.aDoubleSeguro()

        if (capital <= 0.0 || tasaAnual <= 0.0 || meses <= 0) {
            _uiState.update {
                it.copy(
                    cuotaMensual = 0.0,
                    cuotaTotalConAporte = 0.0,
                    totalIntereses = 0.0,
                    totalPagar = 0.0,
                    mesesLiquidados = 0,
                    ahorroIntereses = 0.0,
                    tablaAmortizacion = emptyList()
                )
            }
            return
        }

        val tasaMensual = (tasaAnual / 100) / 12.0
        val factor = (1.0 + tasaMensual).pow(meses)

        val cuotaMensualBase = capital * (tasaMensual * factor) / (factor - 1.0)
        val totalInteresesSinAporte = (cuotaMensualBase * meses) - capital

        var saldoRestante = capital
        val nuevaTabla = mutableListOf<PagoMensual>()

        var sumaIntereses = 0.0
        var sumaTotalPagado = 0.0

        for (mes in 1..meses) {
            var cuotaMesActual = cuotaMensualBase
            var tasaMensualMes = tasaMensual

            if (!estadoActual.esInteresFijo) {
                val tasaAnualDinamica = tasaAnual + (mes * 0.2)
                tasaMensualMes = (tasaAnualDinamica / 100) / 12.0

                val mesesRestantes = meses - mes + 1
                val factorDinamico = (1.0 + tasaMensualMes).pow(mesesRestantes)
                cuotaMesActual = saldoRestante * (tasaMensualMes * factorDinamico) / (factorDinamico - 1.0)
            }

            val interesMes = saldoRestante * tasaMensualMes
            var capitalMes = cuotaMesActual - interesMes

            capitalMes += aporteExtraMensual

            if (capitalMes > saldoRestante) {
                capitalMes = saldoRestante
            }

            saldoRestante -= capitalMes
            if (saldoRestante < 0.01) saldoRestante = 0.0

            val pagoRealEsteMes = capitalMes + interesMes

            sumaIntereses += interesMes
            sumaTotalPagado += pagoRealEsteMes

            nuevaTabla.add(
                PagoMensual(
                    mes = mes,
                    cuota = pagoRealEsteMes,
                    capital = capitalMes,
                    interes = interesMes,
                    saldo = saldoRestante
                )
            )

            if (saldoRestante <= 0.0) break
        }

        val mesesReales = nuevaTabla.size
        val ahorroIntereses = (totalInteresesSinAporte - sumaIntereses).coerceAtLeast(0.0)
        val cuotaTotalConAporte = cuotaMensualBase + aporteExtraMensual

        _uiState.update {
            it.copy(
                cuotaMensual = cuotaMensualBase,
                cuotaTotalConAporte = cuotaTotalConAporte,
                totalIntereses = sumaIntereses,
                totalPagar = sumaTotalPagado,
                mesesLiquidados = mesesReales,
                ahorroIntereses = ahorroIntereses,
                tablaAmortizacion = nuevaTabla
            )
        }
    }
}

class SimuladorViewModelFactory(private val repository: CreditoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimuladorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SimuladorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
