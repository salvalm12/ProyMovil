package com.example.turnofacil.data.repository

import com.example.turnofacil.data.local.entity.AddonEntity
import com.example.turnofacil.data.local.entity.AppointmentAddonCrossRef
import com.example.turnofacil.data.local.entity.AppointmentEntity
import com.example.turnofacil.data.local.entity.ServiceEntity
import com.example.turnofacil.data.local.relation.AppointmentWithDetails
import com.example.turnofacil.data.model.EstadoCita
import com.example.turnofacil.data.model.Servicio
import com.example.turnofacil.domain.model.SyncState
import com.example.turnofacil.domain.model.TipoAtencion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class RepositoryTest {

    private lateinit var fakeLocalDataSource: FakeTestLocalDataSource
    private lateinit var serviceRepository: ServiceRepositoryImpl
    private lateinit var appointmentRepository: AppointmentRepositoryImpl

    @Before
    fun setUp() {
        fakeLocalDataSource = FakeTestLocalDataSource()
        serviceRepository = ServiceRepositoryImpl(fakeLocalDataSource)
        appointmentRepository = AppointmentRepositoryImpl(fakeLocalDataSource)
    }

    @Test
    fun testCitaNuevaNacePendiente() = runTest {
        val servicioId = serviceRepository.agregarServicio(
            Servicio(id = "0", nombre = "Corte Simple", precio = 100.0, duracionMin = 30)
        )

        val solicitud = SolicitudAgendarCita(
            servicioId = servicioId,
            cliente = "Juan Pérez",
            tipoAtencion = TipoAtencion.LOCAL,
            adicionales = emptyList(),
            hora = "10:00",
            fecha = "2026-11-10",
        )

        val resultado = appointmentRepository.agendarCita(solicitud)
        assertEquals(ResultadoAgendar.Exito, resultado)

        val citaGuardada = fakeLocalDataSource.appointments.firstOrNull()
        assertNotNull(citaGuardada)
        assertEquals(EstadoCita.PENDIENTE, citaGuardada?.status)
    }

    @Test
    fun testTotalCitaEsServicioMasAdicionales() = runTest {
        val servicioId = serviceRepository.agregarServicio(
            Servicio(id = "0", nombre = "Corte de Cabello", precio = 150.0, duracionMin = 30)
        )
        fakeLocalDataSource.addons.add(AddonEntity(id = 1L, name = "Barba", price = 80.0, active = true))
        fakeLocalDataSource.addons.add(AddonEntity(id = 2L, name = "Lavado", price = 50.0, active = true))

        val solicitud = SolicitudAgendarCita(
            servicioId = servicioId,
            cliente = "Carlos López",
            tipoAtencion = TipoAtencion.LOCAL,
            adicionales = listOf("Barba", "Lavado"),
            hora = "11:00",
            fecha = "2026-11-10",
        )

        val resultado = appointmentRepository.agendarCita(solicitud)
        assertEquals(ResultadoAgendar.Exito, resultado)

        val citaEntity = fakeLocalDataSource.appointments.first()
        assertEquals(280.0, citaEntity.totalPrice, 0.01)
    }

    @Test
    fun testAgendarHorarioOcupadoDevuelveErrorYNoInserta() = runTest {
        val servicioId = serviceRepository.agregarServicio(
            Servicio(id = "0", nombre = "Tinte", precio = 300.0, duracionMin = 60)
        )

        val solicitud1 = SolicitudAgendarCita(
            servicioId = servicioId,
            cliente = "María",
            tipoAtencion = TipoAtencion.LOCAL,
            hora = "14:00",
            fecha = "2026-11-10",
        )
        val res1 = appointmentRepository.agendarCita(solicitud1)
        assertEquals(ResultadoAgendar.Exito, res1)

        val solicitud2 = SolicitudAgendarCita(
            servicioId = servicioId,
            cliente = "Ana",
            tipoAtencion = TipoAtencion.LOCAL,
            hora = "14:00",
            fecha = "2026-11-10",
        )
        val res2 = appointmentRepository.agendarCita(solicitud2)
        assertEquals(ResultadoAgendar.HorarioOcupado, res2)

        assertEquals(1, fakeLocalDataSource.appointments.size)
    }

    @Test
    fun testReprogramarMismaCitaAPropioHorarioNoDaConflicto() = runTest {
        val servicioId = serviceRepository.agregarServicio(
            Servicio(id = "0", nombre = "Manicura", precio = 120.0, duracionMin = 30)
        )

        val solicitud = SolicitudAgendarCita(
            servicioId = servicioId,
            cliente = "Sofia",
            tipoAtencion = TipoAtencion.LOCAL,
            hora = "15:00",
            fecha = "2026-11-10",
        )
        appointmentRepository.agendarCita(solicitud)
        val citaGuardada = fakeLocalDataSource.appointments.first()

        val resReprogramar = appointmentRepository.reprogramarCita(
            id = citaGuardada.id.toString(),
            nuevaFecha = "2026-11-10",
            nuevaHora = "15:00",
        )

        assertEquals(ResultadoAgendar.Exito, resReprogramar)
    }

    @Test
    fun testCancelarLiberaHorario() = runTest {
        val servicioId = serviceRepository.agregarServicio(
            Servicio(id = "0", nombre = "Masaje", precio = 400.0, duracionMin = 45)
        )

        val solicitud1 = SolicitudAgendarCita(
            servicioId = servicioId,
            cliente = "Pedro",
            tipoAtencion = TipoAtencion.LOCAL,
            hora = "16:00",
            fecha = "2026-11-10",
        )
        appointmentRepository.agendarCita(solicitud1)
        val idCita1 = fakeLocalDataSource.appointments.first().id.toString()

        val cancelado = appointmentRepository.cancelarCita(idCita1)
        assertTrue(cancelado)

        val solicitud2 = SolicitudAgendarCita(
            servicioId = servicioId,
            cliente = "Luis",
            tipoAtencion = TipoAtencion.LOCAL,
            hora = "16:00",
            fecha = "2026-11-10",
        )
        val res2 = appointmentRepository.agendarCita(solicitud2)
        assertEquals(ResultadoAgendar.Exito, res2)
    }

    @Test
    fun testEscrituraDejaSyncStatePendingSync() = runTest {
        val servicioId = serviceRepository.agregarServicio(
            Servicio(id = "0", nombre = "Facial", precio = 250.0, duracionMin = 45)
        )

        val servicioGuardado = fakeLocalDataSource.services.first()
        assertEquals(SyncState.PENDIENTE, servicioGuardado.syncState)

        val solicitud = SolicitudAgendarCita(
            servicioId = servicioId,
            cliente = "Elena",
            tipoAtencion = TipoAtencion.LOCAL,
            hora = "09:00",
            fecha = "2026-11-12",
        )
        appointmentRepository.agendarCita(solicitud)
        val citaGuardada = fakeLocalDataSource.appointments.first()
        assertEquals(SyncState.PENDIENTE, citaGuardada.syncState)
    }

    @Test
    fun testBajaLogicaDejaServicioInactivo() = runTest {
        val servicioId = serviceRepository.agregarServicio(
            Servicio(id = "0", nombre = "Pedicura", precio = 180.0, duracionMin = 40)
        )

        val eliminado = serviceRepository.eliminarServicio(servicioId)
        assertTrue(eliminado)

        val servicioInactivo = fakeLocalDataSource.services.first { it.id.toString() == servicioId }
        assertFalse(servicioInactivo.active)
    }
}

class FakeTestLocalDataSource : LocalDataSource(database = null) {
    val services = mutableListOf<ServiceEntity>()
    val addons = mutableListOf<AddonEntity>()
    val appointments = mutableListOf<AppointmentEntity>()
    val crossRefs = mutableListOf<AppointmentAddonCrossRef>()
    private var idCounter = 1L

    override fun observeAllServices(): Flow<List<ServiceEntity>> = flowOf(services)

    override fun observeActiveServices(): Flow<List<ServiceEntity>> = flowOf(services.filter { it.active })

    override suspend fun getServiceById(id: Long): ServiceEntity? = services.firstOrNull { it.id == id }

    override suspend fun insertService(service: ServiceEntity): Long {
        val newId = if (service.id > 0L) service.id else idCounter++
        val entity = service.copy(id = newId)
        services.removeAll { it.id == newId }
        services.add(entity)
        return newId
    }

    override suspend fun updateService(service: ServiceEntity): Int {
        val index = services.indexOfFirst { it.id == service.id }
        if (index >= 0) {
            services[index] = service
            return 1
        }
        return 0
    }

    override suspend fun softDeleteService(id: Long, updatedAt: Long): Int {
        val index = services.indexOfFirst { it.id == id }
        if (index >= 0) {
            services[index] = services[index].copy(active = false, syncState = SyncState.PENDIENTE, updatedAt = updatedAt)
            return 1
        }
        return 0
    }

    override fun observeAppointmentsWithDetailsByDate(date: LocalDate): Flow<List<AppointmentWithDetails>> {
        val list = appointments.filter { it.date == date }.map { app ->
            val srv = services.first { it.id == app.serviceId }
            val addonIds = crossRefs.filter { it.appointmentId == app.id }.map { it.addonId }
            val addList = addons.filter { it.id in addonIds }
            AppointmentWithDetails(app, srv, addList)
        }
        return flowOf(list)
    }

    override fun observeAppointmentWithDetailsById(id: Long): Flow<AppointmentWithDetails?> {
        val app = appointments.firstOrNull { it.id == id } ?: return flowOf(null)
        val srv = services.firstOrNull { it.id == app.serviceId } ?: return flowOf(null)
        val addonIds = crossRefs.filter { it.appointmentId == app.id }.map { it.addonId }
        val addList = addons.filter { it.id in addonIds }
        return flowOf(AppointmentWithDetails(app, srv, addList))
    }

    override suspend fun getAppointmentWithDetailsById(id: Long): AppointmentWithDetails? {
        val app = appointments.firstOrNull { it.id == id } ?: return null
        val srv = services.firstOrNull { it.id == app.serviceId } ?: return null
        val addonIds = crossRefs.filter { it.appointmentId == app.id }.map { it.addonId }
        val addList = addons.filter { it.id in addonIds }
        return AppointmentWithDetails(app, srv, addList)
    }

    override suspend fun insertAppointment(appointment: AppointmentEntity): Long {
        val newId = if (appointment.id > 0L) appointment.id else idCounter++
        val entity = appointment.copy(id = newId)
        appointments.add(entity)
        return newId
    }

    override suspend fun insertAppointmentAddonCrossRefs(crossRefs: List<AppointmentAddonCrossRef>) {
        this.crossRefs.addAll(crossRefs)
    }

    override suspend fun updateAppointmentStatus(id: Long, status: EstadoCita, updatedAt: Long): Int {
        val index = appointments.indexOfFirst { it.id == id }
        if (index >= 0) {
            appointments[index] = appointments[index].copy(status = status, syncState = SyncState.PENDIENTE, updatedAt = updatedAt)
            return 1
        }
        return 0
    }

    override suspend fun rescheduleAppointment(id: Long, date: LocalDate, time: LocalTime, updatedAt: Long): Int {
        val index = appointments.indexOfFirst { it.id == id }
        if (index >= 0) {
            appointments[index] = appointments[index].copy(date = date, time = time, syncState = SyncState.PENDIENTE, updatedAt = updatedAt)
            return 1
        }
        return 0
    }

    override suspend fun cancelAppointment(id: Long, updatedAt: Long): Int {
        return updateAppointmentStatus(id, EstadoCita.CANCELADA, updatedAt)
    }

    override suspend fun isSlotBusy(date: LocalDate, time: LocalTime): Boolean {
        return appointments.any {
            it.date == date && it.time == time && (it.status == EstadoCita.PENDIENTE || it.status == EstadoCita.CONFIRMADA)
        }
    }

    override suspend fun isSlotBusyExcluding(date: LocalDate, time: LocalTime, excludeId: Long): Boolean {
        return appointments.any {
            it.id != excludeId && it.date == date && it.time == time && (it.status == EstadoCita.PENDIENTE || it.status == EstadoCita.CONFIRMADA)
        }
    }

    override fun observeActiveAddons(): Flow<List<AddonEntity>> = flowOf(addons.filter { it.active })

    override suspend fun getAddonByName(name: String): AddonEntity? = addons.firstOrNull { it.name.equals(name, ignoreCase = true) }

    override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
}
