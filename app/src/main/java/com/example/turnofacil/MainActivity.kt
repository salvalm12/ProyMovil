package com.example.turnofacil

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.example.turnofacil.app.LocalAppContainer
import com.example.turnofacil.app.TurnoFacilApplication
import com.example.turnofacil.data.prefs.PreferenciasApp
import com.example.turnofacil.ui.theme.TurnoFacilTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(PreferenciasApp.contextConIdioma(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as TurnoFacilApplication).container

        setContent {
            CompositionLocalProvider(LocalAppContainer provides appContainer) {
                TurnoFacilTheme {
                    TurnoFacilApp()
                }
            }
        }
    }
}
