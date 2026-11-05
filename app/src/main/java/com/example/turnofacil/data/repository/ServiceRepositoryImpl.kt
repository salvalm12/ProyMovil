package com.example.turnofacil.data.repository

import com.example.turnofacil.data.local.entity.AddonEntity
import com.example.turnofacil.data.local.entity.ServiceEntity
import com.example.turnofacil.data.model.Servicio
import com.example.turnofacil.domain.model.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementación de [ServiceRepository] sobre [LocalDataSource].
 * Toda modificación local se guarda marcando [SyncState.PENDIENTE].
 */
class ServiceRepositoryImpl(
    private val localDataSource: LocalDataSource,
) : ServiceRepository {

    override fun getServicios(): Flow<List<Servicio>> {
        return localDataSource.observeAllServices().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getServiciosActivos(): Flow<List<Servicio>> {
        return localDataSource.observeActiveServices().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getServicioPorId(id: String): Servicio? {
        val idLong = id.toLongOrNull() ?: return null
        return localDataSource.getServiceById(idLong)?.toDomain()
    }

    override suspend fun agregarServicio(servicio: Servicio): String {
        val ahora = System.currentTimeMillis()
        val entidad = servicio.toEntity(
            syncState = SyncState.PENDIENTE,
            updatedAt = ahora,
        )
        val insertedId = localDataSource.insertService(entidad)
        return insertedId.toString()
    }

    override suspend fun actualizarServicio(servicio: Servicio) {
        val idLong = servicio.id.toLongOrNull()
        val ahora = System.currentTimeMillis()
        if (idLong != null && idLong > 0L) {
            val existente = localDataSource.getServiceById(idLong)
            val entidad = if (existente != null) {
                existente.copy(
                    name = servicio.nombre,
                    price = servicio.precio,
                    durationMin = servicio.duracionMin,
                    active = servicio.activo,
                    syncState = SyncState.PENDIENTE,
                    updatedAt = ahora,
                )
            } else {
                servicio.toEntity(syncState = SyncState.PENDIENTE, updatedAt = ahora)
            }
            localDataSource.updateService(entidad)
        } else {
            localDataSource.insertService(servicio.toEntity(syncState = SyncState.PENDIENTE, updatedAt = ahora))
        }
    }

    override suspend fun eliminarServicio(id: String): Boolean {
        val idLong = id.toLongOrNull() ?: return false
        val ahora = System.currentTimeMillis()
        return localDataSource.softDeleteService(idLong, ahora) > 0
    }

    override fun getAdicionales(): Flow<List<Adicional>> {
        return localDataSource.observeActiveAddons().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}

fun ServiceEntity.toDomain(): Servicio = Servicio(
    id = id.toString(),
    nombre = name,
    precio = price,
    duracionMin = durationMin,
    activo = active,
)

fun Servicio.toEntity(
    syncState: SyncState = SyncState.PENDIENTE,
    updatedAt: Long = System.currentTimeMillis(),
): ServiceEntity {
    val idLong = id.toLongOrNull() ?: 0L
    return ServiceEntity(
        id = idLong,
        remoteId = if (idLong == 0L && id.isNotBlank()) id else null,
        name = nombre,
        price = precio,
        durationMin = duracionMin,
        active = activo,
        syncState = syncState,
        updatedAt = updatedAt,
    )
}

fun AddonEntity.toDomain(): Adicional = Adicional(
    id = id.toString(),
    nombre = name,
    precio = price,
    activo = active,
)
