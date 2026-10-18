package com.upchiapas.kt_template

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.kt_template.data.AppDatabase
import com.upchiapas.kt_template.ui.components.MainScreen
import com.upchiapas.kt_template.ui.theme.Kt_templateTheme
import com.upchiapas.kt_template.viewmodel.SimuladorViewModel
import com.upchiapas.kt_template.viewmodel.SimuladorViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Kt_templateTheme {
                val context = LocalContext.current
                // Instanciamos la base de datos
                val database = AppDatabase.obtenerBaseDeDatos(context)

                // Le pasamos el DAO al ViewModel a través de nuestro Factory
                val simuladorViewModel: SimuladorViewModel = viewModel(
                    factory = SimuladorViewModelFactory(database.creditoDao())
                )

                // Llamamos a la pantalla pasándole el ViewModel configurado
                MainScreen(viewModel = simuladorViewModel)
            }
        }
    }
}
