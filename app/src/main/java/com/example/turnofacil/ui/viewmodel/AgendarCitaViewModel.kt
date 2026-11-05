package com.example.turnofacil.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.EstadoCita
import com.example.turnofacil.data.model.Servicio
import com.example.turnofacil.data.model.TipoAtencion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AgendarCitaViewModel(
    private val servicioRepositorio: ServicioRepositorio,
    private val citaRepositorio: CitaRepositorio,
    servicioIdInicial: String? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendarCitaUiState(cargando = true))
    val uiState: StateFlow<AgendarCitaUiState> = _uiState.asStateFlow()

    init {
        cargarDatos(servicioIdInicial)
    }

    private fun cargarDatos(servicioIdInicial: String?) {
        viewModelScope.launch {
            combine(
                servicioRepositorio.observarServicios(),
                servicioRepositorio.observarAdicionales()
            ) { servicios, adicionales ->
                val activos = servicios.filter { it.activo }
                val seleccionado = if (!servicioIdInicial.isNullOrEmpty()) {
                    activos.firstOrNull { it.id == servicioIdInicial } ?: activos.firstOrNull()
                } else {
                    activos.firstOrNull()
                }
                Pair(activos, adicionales to seleccionado)
            }.collect { (servicios, adicionalesYSeleccionado) ->
                val (adicionales, seleccionado) = adicionalesYSeleccionado
                _uiState.update { actual ->
                    actual.copy(
                        serviciosDisponibles = servicios,
                        adicionalesDisponibles = adicionales,
                        servicioSeleccionado = actual.servicioSeleccionado ?: seleccionado,
                        cargando = false
                    )
                }
            }
        }
    }

    fun onNombreChange(nuevoNombre: String) {
        _uiState.update { it.copy(nombreCliente = nuevoNombre, errorNombreVacio = false) }
    }

    fun onServicioChange(servicio: Servicio) {
        _uiState.update { it.copy(servicioSeleccionado = servicio) }
    }

    fun onTipoAtencionChange(tipo: TipoAtencion) {
        _uiState.update { it.copy(tipoAtencion = tipo) }
    }

    fun onAdicionalToggle(adicional: Adicional) {
        _uiState.update { actual ->
            val seleccionados = actual.adicionalesSeleccionados.toMutableSet()
            if (seleccionados.contains(adicional)) {
                seleccionados.remove(adicional)
            } else {
                seleccionados.add(adicional)
            }
            actual.copy(adicionalesSeleccionados = seleccionados)
        }
    }

    fun onHoraChange(nuevaHora: String) {
        _uiState.update {
            it.copy(
                hora = nuevaHora,
                errorHoraFueraHorario = false,
                errorHorarioOcupado = false
            )
        }
    }

    fun enviar() {
        val estadoActual = _uiState.value

        val nombreVacio = estadoActual.nombreCliente.trim().isEmpty()
        val horaValida = estaEnHorarioAtencion(
            hora = estadoActual.hora,
            apertura = estadoActual.horaApertura,
            cierre = estadoActual.horaCierre
        )

        if (nombreVacio || !horaValida) {
            _uiState.update {
                it.copy(
                    errorNombreVacio = nombreVacio,
                    errorHoraFueraHorario = !horaValida
                )
            }
            return
        }

        val servicio = estadoActual.servicioSeleccionado ?: return

        _uiState.update { it.copy(guardando = true, errorHorarioOcupado = false) }

        viewModelScope.launch {
            val nuevaCita = Cita(
                id = "",
                cliente = estadoActual.nombreCliente.trim(),
                servicioId = servicio.id,
                tipoAtencion = estadoActual.tipoAtencion,
                adicionales = estadoActual.adicionalesSeleccionados.map { it.nombre },
                hora = estadoActual.hora,
                fecha = estadoActual.fecha,
                estado = EstadoCita.PENDIENTE
            )

            when (citaRepositorio.agendar(nuevaCita)) {
                ResultadoAgendar.Exito -> {
                    _uiState.update { it.copy(guardando = false, guardadoExitoso = true) }
                }

                ResultadoAgendar.HorarioOcupado -> {
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            errorHorarioOcupado = true
                        )
                    }
                }
            }
        }
    }

    private fun estaEnHorarioAtencion(hora: String, apertura: String, cierre: String): Boolean {
        val minHora = horaAMinutos(hora) ?: return false
        val minApertura = horaAMinutos(apertura) ?: return false
        val minCierre = horaAMinutos(cierre) ?: return false

        // Regla 4: abre <= hora < cierra
        return minHora in minApertura..<minCierre
    }

    private fun horaAMinutos(hora: String): Int? {
        val partes = hora.split(":")
        if (partes.size != 2) return null
        val h = partes[0].toIntOrNull() ?: return null
        val m = partes[1].toIntOrNull() ?: return null
        return h * 60 + m
    }
}
