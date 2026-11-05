package com.example.turnofacil.data.model

/**
 * Alias de compatibilidad para las pantallas existentes.
 *
 * Los modelos de dominio viven en [com.example.turnofacil.domain.model] (como
 * pide la guía de la capa de datos); estos alias permiten que el código de UI
 * que ya importaba `data.model` siga compilando sin cambios.
 */
typealias TipoAtencion = com.example.turnofacil.domain.model.TipoAtencion
typealias EstadoCita = com.example.turnofacil.domain.model.EstadoCita
typealias Servicio = com.example.turnofacil.domain.model.Servicio
typealias Cita = com.example.turnofacil.domain.model.Cita
