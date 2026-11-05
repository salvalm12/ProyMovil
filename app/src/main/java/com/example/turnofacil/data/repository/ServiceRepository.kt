package com.example.turnofacil.data.repository

import com.example.turnofacil.data.model.Servicio
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para la administración de servicios y adicionales.
 */
interface ServiceRepository {
    /** Observa todos los servicios (activos e inactivos). */
    fun getServicios(): Flow<List<Servicio>>

    /** Observa únicamente los servicios activos. */
    fun getServiciosActivos(): Flow<List<Servicio>>

    /** Obtiene un servicio específico por su id de dominio. */
    suspend fun getServicioPorId(id: String): Servicio?

    /** Agrega o inserta un nuevo servicio y devuelve su id. */
    suspend fun agregarServicio(servicio: Servicio): String

    /** Actualiza los datos de un servicio existente. */
    suspend fun actualizarServicio(servicio: Servicio)

    /** Realiza la baja lógica del servicio (lo marca inactivo). */
    suspend fun eliminarServicio(id: String): Boolean

    /** Observa la lista de adicionales activos disponibles. */
    fun getAdicionales(): Flow<List<Adicional>>
}
