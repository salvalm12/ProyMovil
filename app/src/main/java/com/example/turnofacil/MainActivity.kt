package com.example.turnofacil

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.turnofacil.data.prefs.PreferenciasApp
import com.example.turnofacil.ui.theme.TurnoFacilTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Aplica el idioma elegido en Configuración (o el del sistema si es "sistema").
        super.attachBaseContext(PreferenciasApp.contextConIdioma(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TurnoFacilTheme {
                TurnoFacilApp()
            }
        }
    }
}
