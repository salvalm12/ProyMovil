package com.example.turnofacil.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ServiciosViewModel(
    private val servicioRepositorio: ServicioRepositorio,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiciosUiState(cargando = true))
    val uiState: StateFlow<ServiciosUiState> = _uiState.asStateFlow()

    init {
        cargarServicios()
    }

    private fun cargarServicios() {
        viewModelScope.launch {
            servicioRepositorio.observarServicios()
                .map { lista ->
                    val activos = lista.filter { it.activo }
                    ServiciosUiState(servicios = activos, cargando = false, error = null)
                }
                .catch { throwable ->
                    emit(
                        ServiciosUiState(
                            servicios = emptyList(),
                            cargando = false,
                            error = throwable.message ?: "Error al cargar servicios",
                        )
                    )
                }
                .collect { estado ->
                    _uiState.value = estado
                }
        }
    }
}
