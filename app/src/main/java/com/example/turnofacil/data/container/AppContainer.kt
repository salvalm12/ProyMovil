package com.example.turnofacil.data.container

import com.example.turnofacil.data.repository.AppointmentRepository
import com.example.turnofacil.data.repository.FakeAppointmentRepository
import com.example.turnofacil.data.repository.FakeServiceRepository
import com.example.turnofacil.data.repository.ServiceRepository

/**
 * Contenedor de Inyección de Dependencias Manual (sin Hilt).
 */
interface AppContainer {
    val serviceRepository: ServiceRepository
    val appointmentRepository: AppointmentRepository
}

/**
 * Implementación predeterminada de [AppContainer].
 */
class DefaultAppContainer : AppContainer {
    override val serviceRepository: ServiceRepository by lazy {
        FakeServiceRepository()
    }

    override val appointmentRepository: AppointmentRepository by lazy {
        FakeAppointmentRepository(serviceRepository)
    }
}
