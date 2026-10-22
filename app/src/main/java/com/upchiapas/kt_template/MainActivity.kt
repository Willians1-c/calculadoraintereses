package com.upchiapas.kt_template

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.kt_template.data.AppDatabase
import com.upchiapas.kt_template.data.repository.CreditoRepositoryImpl
import com.upchiapas.kt_template.ui.theme.Kt_templateTheme
import com.upchiapas.kt_template.uiview.MainScreen
import com.upchiapas.kt_template.viewmodel.SimuladorViewModel
import com.upchiapas.kt_template.viewmodel.SimuladorViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkTheme by remember { mutableStateOf(false) }

            Kt_templateTheme(darkTheme = darkTheme) {
                val context = LocalContext.current
                // Instanciamos la base de datos
                val database = AppDatabase.obtenerBaseDeDatos(context)

                // Creamos el repositorio
                val repository = CreditoRepositoryImpl(database.creditoDao())

                // Le pasamos el repositorio al ViewModel a través de nuestro Factory
                val simuladorViewModel: SimuladorViewModel = viewModel(
                    factory = SimuladorViewModelFactory(repository)
                )

                // Llamamos a la pantalla pasándole el ViewModel y el control de tema
                MainScreen(
                    viewModel = simuladorViewModel,
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = it }
                )
            }
        }
    }
}
