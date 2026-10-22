package com.upchiapas.kt_template.uiview

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.kt_template.viewmodel.SimuladorViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: SimuladorViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    val historialCreditos by viewModel.historialCreditos.collectAsState()
    val contexto = LocalContext.current
    var showHistorial by remember { mutableStateOf(false) }

    if (showHistorial) {
        HistorialScreen(
            historial = historialCreditos,
            onBorrarHistorial = { viewModel.borrarHistorial() },
            onBack = { showHistorial = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("CrediCalc", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { showHistorial = true }) {
                            Icon(Icons.Default.List, contentDescription = "Ver Historial")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // --- 1. SECCIÓN DE ENTRADAS ---
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = estado.monto,
                        onValueChange = { viewModel.actualizarMonto(it) },
                        label = { Text("Monto del préstamo") },
                        leadingIcon = { Text("$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = estado.tasa,
                        onValueChange = { viewModel.actualizarTasa(it) },
                        label = { Text("Tasa anual (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = estado.aporteExtra,
                        onValueChange = { viewModel.actualizarAporteExtra(it) },
                        label = { Text("Aporte extra") },
                        leadingIcon = { Text("$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- SLIDER DE PLAZO ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Plazo del préstamo", fontWeight = FontWeight.Bold)
                    Text("${estado.plazoMeses.toInt()} meses", fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = estado.plazoMeses,
                    onValueChange = { viewModel.actualizarPlazo(it) },
                    valueRange = 6f..360f,
                    steps = 354
                )

                // --- TIPO DE INTERÉS ---
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tipo de interés", color = Color.Gray, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = estado.esInteresFijo,
                        onClick = { viewModel.alternarTipoInteres(true) },
                        label = { Text("Fijo") }
                    )
                    FilterChip(
                        selected = !estado.esInteresFijo,
                        onClick = { viewModel.alternarTipoInteres(false) },
                        label = { Text("Variable") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- BOTONES: CALCULAR Y GUARDAR ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.calcularAmortizacion() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Calcular")
                    }

                    // Botón para Guardar en Base de Datos y abrir el historial
                    IconButton(
                        onClick = {
                            viewModel.guardarSimulacion()
                            Toast.makeText(contexto, "Simulación guardada en el historial", Toast.LENGTH_SHORT).show()
                            showHistorial = true
                        },
                        modifier = Modifier.background(Color(0xFFE3F2FD), shape = RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Guardar simulación",
                            tint = Color(0xFF1976D2)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val capitalIngresado = estado.monto.toDoubleOrNull() ?: 0.0
                val esPeligroso = capitalIngresado > 0 && estado.totalIntereses > (capitalIngresado * 0.5)

                val colorFondoTarjeta = if (esPeligroso) Color(0xFFFFEBEE) else Color(0xFFF9F9F9)
                val colorTextoAlerta = if (esPeligroso) Color(0xFFD32F2F) else Color.Gray
                val mensajeAlerta = if (esPeligroso) "RIESGO: Intereses > 50% del capital" else "✓ Intereses en nivel saludable"

                // 2. TARJETA DE RESUMEN
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colorFondoTarjeta)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cuota mensual inicial", color = Color.Gray, fontSize = 14.sp)
                        Text(String.format(Locale.US, "$%.2f", estado.cuotaMensual), fontSize = 32.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total intereses", color = Color.Gray, fontSize = 14.sp)
                                Text(String.format(Locale.US, "$%.2f", estado.totalIntereses), fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Total a pagar", color = Color.Gray, fontSize = 14.sp)
                                Text(String.format(Locale.US, "$%.2f", estado.totalPagar), fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Chip de advertencia
                        Surface(
                            color = colorFondoTarjeta,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colorTextoAlerta),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = mensajeAlerta,
                                color = colorTextoAlerta,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. TABLA DE AMORTIZACIÓN
                if (estado.tablaAmortizacion.isNotEmpty()) {
                    Text("Tabla de amortización", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        estado.tablaAmortizacion.forEach { pago ->
                            FilaAmortizacionItem(
                                mes = pago.mes,
                                cuota = String.format(Locale.US, "%.2f", pago.cuota),
                                cap = String.format(Locale.US, "%.2f", pago.capital),
                                int = String.format(Locale.US, "%.2f", pago.interes),
                                saldo = String.format(Locale.US, "%.2f", pago.saldo)
                            )
                            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun FilaAmortizacionItem(mes: Int, cuota: String, cap: String, int: String, saldo: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFFEEEEEE),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(mes.toString(), color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text("Cuota $$cuota", fontWeight = FontWeight.Medium, color = Color.DarkGray)
            Text("Cap. $$cap · Int. $$int", fontSize = 12.sp, color = Color.Gray)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("Saldo", fontSize = 12.sp, color = Color.Gray)
            Text("$$saldo", fontWeight = FontWeight.Medium, color = Color.DarkGray)
        }
    }
}
