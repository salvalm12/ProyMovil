package com.example.turnofacil.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.turnofacil.data.prefs.PreferenciasApp
import com.example.turnofacil.data.prefs.buscarActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel para la pantalla de Configuración (RF9 y RNF3).
 * Gestiona el idioma de la aplicación (es/en/sistema), el horario de atención
 * y las preferencias de sincronización.
 */
class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun cargarPreferencias(context: Context) {
        val idiomaActual = PreferenciasApp.idioma(context)
        _uiState.update { it.copy(idioma = idiomaActual) }
    }

    fun cambiarIdioma(context: Context, nuevoIdioma: String) {
        if (nuevoIdioma != _uiState.value.idioma) {
            PreferenciasApp.establecerIdioma(context, nuevoIdioma)
            _uiState.update { it.copy(idioma = nuevoIdioma) }
            context.buscarActivity()?.recreate()
        }
    }

    fun actualizarHoraApertura(hora: String) {
        _uiState.update { it.copy(horaApertura = hora, mensajeExito = "settings_horario_guardado") }
    }

    fun actualizarHoraCierre(hora: String) {
        _uiState.update { it.copy(horaCierre = hora, mensajeExito = "settings_horario_guardado") }
    }

    fun toggleSyncAutomatica(activada: Boolean) {
        _uiState.update { it.copy(syncAutomatica = activada) }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(mensajeExito = null) }
    }
}
