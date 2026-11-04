package com.example.turnofacil.ui.viewmodel

import com.example.turnofacil.data.model.Servicio

data class ServiciosUiState(
    val servicios: List<Servicio> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null,
)
