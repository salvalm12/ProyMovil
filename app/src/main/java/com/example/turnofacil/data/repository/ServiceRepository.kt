package com.example.turnofacil.data.repository

import com.example.turnofacil.data.model.Servicio
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para la administración de servicios.
 */
interface ServiceRepository {
    fun getServicios(): Flow<List<Servicio>>
    suspend fun getServicioPorId(id: String): Servicio?
    suspend fun agregarServicio(servicio: Servicio)
    suspend fun actualizarServicio(servicio: Servicio)
    suspend fun eliminarServicio(id: String): Boolean
}
