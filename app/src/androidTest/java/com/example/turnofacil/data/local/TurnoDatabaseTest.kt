package com.example.turnofacil.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.turnofacil.data.local.dao.AddonDao
import com.example.turnofacil.data.local.dao.AppointmentDao
import com.example.turnofacil.data.local.dao.MediaDao
import com.example.turnofacil.data.local.dao.NotificationDao
import com.example.turnofacil.data.local.dao.ServiceDao
import com.example.turnofacil.data.local.entity.AddonEntity
import com.example.turnofacil.data.local.entity.AppointmentAddonCrossRef
import com.example.turnofacil.data.local.entity.AppointmentEntity
import com.example.turnofacil.data.local.entity.MediaEntity
import com.example.turnofacil.data.local.entity.NotificationEntity
import com.example.turnofacil.data.local.entity.ServiceEntity
import com.example.turnofacil.domain.model.EstadoCita
import com.example.turnofacil.domain.model.TipoAtencion
import com.example.turnofacil.domain.model.TipoNotificacion
import com.example.turnofacil.domain.model.TipoPropietarioMedia
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class TurnoDatabaseTest {

    private lateinit var db: TurnoDatabase
    private lateinit var serviceDao: ServiceDao
    private lateinit var addonDao: AddonDao
    private lateinit var appointmentDao: AppointmentDao
    private lateinit var notificationDao: NotificationDao
    private lateinit var mediaDao: MediaDao

    private val fecha = LocalDate.of(2026, 1, 15)
    private val ahora = 1_767_000_000_000L

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TurnoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        serviceDao = db.serviceDao()
        addonDao = db.addonDao()
        appointmentDao = db.appointmentDao()
        notificationDao = db.notificationDao()
        mediaDao = db.mediaDao()

        // Precalienta Room: la primera invocación compila las queries y abriría
        // la base dentro del tiempo medido por cada @Test (timeout = 1 s).
        runBlocking {
            serviceDao.observeActive().first()
            serviceDao.observeAll().first()
            serviceDao.getById(0L)
            serviceDao.update(ServiceEntity(name = "warmup", price = 0.0, durationMin = 0))
            serviceDao.softDelete(0L, ahora)

            addonDao.observeActive().first()
            addonDao.observeAll().first()
            addonDao.getById(0L)
            addonDao.update(AddonEntity(name = "warmup", price = 0.0))
            addonDao.softDelete(0L)

            appointmentDao.observeByDate(fecha).first()
            appointmentDao.getById(0L)
            appointmentDao.updateStatus(0L, EstadoCita.PENDIENTE, ahora)
            appointmentDao.reschedule(0L, fecha, LocalTime.parse("00:00"), ahora)
            appointmentDao.cancel(0L, ahora)
            appointmentDao.isSlotBusy(fecha, LocalTime.parse("00:00"))
            appointmentDao.isSlotBusyExcluding(fecha, LocalTime.parse("00:00"), 0L)
            appointmentDao.getWithDetailsById(0L)
            appointmentDao.observeWithDetailsByDate(fecha).first()

            notificationDao.observeByAppointment(0L).first()
            notificationDao.getById(0L)
            notificationDao.deleteById(0L)

            mediaDao.observeByOwner(TipoPropietarioMedia.CITA, 0L).first()
            mediaDao.getById(0L)
            mediaDao.deleteById(0L)

            // Inserts/relaciones que no se pueden precalentar con ids inexistentes:
            // se crean datos temporales y luego se limpia la base.
            val warmServiceId = serviceDao.insert(
                ServiceEntity(name = "warmup", price = 1.0, durationMin = 1),
            )
            val warmAddonId = addonDao.insert(AddonEntity(name = "warmup", price = 1.0))
            val warmCitaId = appointmentDao.insert(
                AppointmentEntity(
                    serviceId = warmServiceId,
                    customerName = "warmup",
                    attentionType = TipoAtencion.LOCAL,
                    date = fecha,
                    time = LocalTime.parse("00:01"),
                    totalPrice = 1.0,
                ),
            )
            appointmentDao.insertAddonCrossRefs(
                listOf(
                    AppointmentAddonCrossRef(
                        appointmentId = warmCitaId,
                        addonId = warmAddonId,
                    ),
                ),
            )
            appointmentDao.getWithDetailsById(warmCitaId)
            appointmentDao.observeWithDetailsByDate(fecha).first()
        }
        db.clearAllTables()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test(timeout = 1_000)
    fun serviceDao_inserta_actualiza_y_hace_baja_logica() = runBlocking {
        val id = serviceDao.insert(nuevoServicio())

        val guardado = serviceDao.getById(id)
        assertNotNull(guardado)
        assertEquals("Corte", guardado!!.name)
        assertTrue(guardado.active)

        assertEquals(1, serviceDao.update(guardado.copy(name = "Corte premium", price = 200.0)))
        assertEquals("Corte premium", serviceDao.getById(id)!!.name)

        assertEquals(1, serviceDao.softDelete(id, ahora))

        val inactivo = serviceDao.getById(id)
        assertNotNull(inactivo)
        assertFalse(inactivo!!.active)
        assertTrue(serviceDao.observeActive().first().isEmpty())
        assertEquals(1, serviceDao.observeAll().first().size)
    }

    @Test(timeout = 1_000)
    fun baja_logica_de_servicio_con_citas_conserva_la_cita() = runBlocking {
        val servicioId = serviceDao.insert(nuevoServicio())
        val citaId = appointmentDao.insert(nuevaCita(servicioId))

        assertEquals(1, serviceDao.softDelete(servicioId, ahora))

        assertFalse(serviceDao.getById(servicioId)!!.active)
        assertNotNull(appointmentDao.getById(citaId))
    }

    @Test(timeout = 1_000)
    fun addonDao_inserta_actualiza_y_hace_baja_logica() = runBlocking {
        val id = addonDao.insert(AddonEntity(name = "Lavado", price = 30.0))

        val guardado = addonDao.getById(id)
        assertNotNull(guardado)
        assertEquals("Lavado", guardado!!.name)

        assertEquals(1, addonDao.update(guardado.copy(name = "Lavado premium", price = 45.0)))
        assertEquals("Lavado premium", addonDao.getById(id)!!.name)

        assertEquals(1, addonDao.softDelete(id))

        val inactivo = addonDao.getById(id)
        assertNotNull(inactivo)
        assertFalse(inactivo!!.active)
        assertTrue(addonDao.observeActive().first().isEmpty())
        assertEquals(1, addonDao.observeAll().first().size)
    }

    @Test(timeout = 1_000)
    fun appointmentDao_inserta_edita_estado_y_hora_y_cancela() = runBlocking {
        val servicioId = serviceDao.insert(nuevoServicio())
        val citaId = appointmentDao.insert(nuevaCita(servicioId))

        val guardada = appointmentDao.getById(citaId)
        assertNotNull(guardada)
        assertEquals(EstadoCita.PENDIENTE, guardada!!.status)

        assertEquals(
            1,
            appointmentDao.updateStatus(citaId, EstadoCita.CONFIRMADA, ahora),
        )
        assertEquals(EstadoCita.CONFIRMADA, appointmentDao.getById(citaId)!!.status)

        val nuevaHora = LocalTime.parse("10:30")
        assertEquals(1, appointmentDao.reschedule(citaId, fecha, nuevaHora, ahora))
        assertEquals(nuevaHora, appointmentDao.getById(citaId)!!.time)

        assertEquals(1, appointmentDao.cancel(citaId, ahora))
        assertEquals(EstadoCita.CANCELADA, appointmentDao.getById(citaId)!!.status)
    }

    @Test(timeout = 1_000)
    fun horario_ocupado_detecta_conflicto_y_cancelada_lo_libera() = runBlocking {
        val servicioId = serviceDao.insert(nuevoServicio())
        val hora = LocalTime.parse("09:00")
        val citaId = appointmentDao.insert(nuevaCita(servicioId, hora.toString()))

        assertTrue(appointmentDao.isSlotBusy(fecha, hora))
        assertTrue(appointmentDao.isSlotBusyExcluding(fecha, hora, excludeId = -1L))
        assertFalse(appointmentDao.isSlotBusyExcluding(fecha, hora, excludeId = citaId))

        appointmentDao.updateStatus(citaId, EstadoCita.CONFIRMADA, ahora)
        assertTrue(appointmentDao.isSlotBusy(fecha, hora))

        appointmentDao.cancel(citaId, ahora)
        assertFalse(appointmentDao.isSlotBusy(fecha, hora))
    }

    @Test(timeout = 1_000)
    fun agenda_por_fecha_ordena_por_hora() = runBlocking {
        val servicioId = serviceDao.insert(nuevoServicio())

        appointmentDao.insert(nuevaCita(servicioId, "11:00"))
        appointmentDao.insert(nuevaCita(servicioId, "09:00"))
        appointmentDao.insert(nuevaCita(servicioId, "10:00"))
        appointmentDao.insert(nuevaCita(servicioId, "08:00", fechaCita = fecha.plusDays(1)))

        val horas = appointmentDao.observeByDate(fecha).first().map { it.time.toString() }

        assertEquals(listOf("09:00", "10:00", "11:00"), horas)
    }

    @Test(timeout = 1_000)
    fun detalle_carga_servicio_y_adicionales_correctos() = runBlocking {
        val servicioId = serviceDao.insert(nuevoServicio(nombre = "Corte + barba", precio = 220.0))
        val lavadoId = addonDao.insert(AddonEntity(name = "Lavado", price = 30.0))
        val barbaId = addonDao.insert(AddonEntity(name = "Barba", price = 40.0))
        val citaId = appointmentDao.insert(nuevaCita(servicioId))

        appointmentDao.insertAddonCrossRefs(
            listOf(
                AppointmentAddonCrossRef(appointmentId = citaId, addonId = lavadoId),
                AppointmentAddonCrossRef(appointmentId = citaId, addonId = barbaId),
            ),
        )

        val detalle = appointmentDao.getWithDetailsById(citaId)
        assertNotNull(detalle)
        assertEquals("Corte + barba", detalle!!.service.name)
        assertEquals(listOf("Barba", "Lavado"), detalle.addons.map { it.name }.sorted())

        val agenda = appointmentDao.observeWithDetailsByDate(fecha).first()
        assertEquals(1, agenda.size)
        assertEquals(citaId, agenda.single().appointment.id)
    }

    @Test(timeout = 1_000)
    fun notificacion_y_media_insertan_consultan_y_borran() = runBlocking {
        val servicioId = serviceDao.insert(nuevoServicio())
        val citaId = appointmentDao.insert(nuevaCita(servicioId))

        val notificacionId = notificationDao.insert(
            NotificationEntity(
                appointmentId = citaId,
                type = TipoNotificacion.RECORDATORIO,
                scheduledAt = ahora,
                message = "Tu cita es mañana",
            ),
        )
        assertNotNull(notificationDao.getById(notificacionId))
        assertEquals(1, notificationDao.observeByAppointment(citaId).first().size)
        assertEquals(1, notificationDao.deleteById(notificacionId))
        assertNull(notificationDao.getById(notificacionId))

        val mediaId = mediaDao.insert(
            MediaEntity(
                ownerType = TipoPropietarioMedia.CITA,
                ownerId = citaId,
                uri = "content://turnofacil/foto.jpg",
                mimeType = "image/jpeg",
            ),
        )
        assertNotNull(mediaDao.getById(mediaId))
        assertEquals(
            1,
            mediaDao.observeByOwner(TipoPropietarioMedia.CITA, citaId).first().size,
        )
        assertEquals(1, mediaDao.deleteById(mediaId))
        assertNull(mediaDao.getById(mediaId))
    }

    private fun nuevoServicio(
        nombre: String = "Corte",
        precio: Double = 150.0,
    ): ServiceEntity = ServiceEntity(
        name = nombre,
        price = precio,
        durationMin = 30,
    )

    private fun nuevaCita(
        serviceId: Long,
        hora: String = "09:00",
        estado: EstadoCita = EstadoCita.PENDIENTE,
        fechaCita: LocalDate = fecha,
    ): AppointmentEntity = AppointmentEntity(
        serviceId = serviceId,
        customerName = "Ana López",
        attentionType = TipoAtencion.LOCAL,
        date = fechaCita,
        time = LocalTime.parse(hora),
        status = estado,
        totalPrice = 150.0,
    )
}
