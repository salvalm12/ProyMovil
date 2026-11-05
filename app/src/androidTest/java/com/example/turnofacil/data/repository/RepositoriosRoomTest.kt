package com.example.turnofacil.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.turnofacil.data.local.TurnoDatabase
import com.example.turnofacil.domain.model.Cita
import com.example.turnofacil.domain.model.EstadoCita
import com.example.turnofacil.domain.model.Servicio
import com.example.turnofacil.domain.model.TipoAtencion
import com.example.turnofacil.ui.viewmodel.ResultadoAgendar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RepositoriosRoomTest {

    private lateinit var db: TurnoDatabase
    private lateinit var servicioRepositorio: ServicioRepositorioRoom
    private lateinit var citaRepositorio: CitaRepositorioRoom
    private lateinit var seeder: RoomSeeder

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TurnoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        servicioRepositorio = ServicioRepositorioRoom(db)
        citaRepositorio = CitaRepositorioRoom(db)
        seeder = RoomSeeder(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test(timeout = 5_000)
    fun seedIfEmpty_carga_datos_demo_y_es_idempotente() = runBlocking {
        seeder.seedIfEmpty()
        seeder.seedIfEmpty()

        val servicios = servicioRepositorio.observarServicios().first()
        val adicionales = servicioRepositorio.observarAdicionales().first()
        val citas = citaRepositorio.observarCitas("Hoy").first()

        assertEquals(5, servicios.size)
        assertEquals(4, adicionales.size)
        assertEquals(4, citas.size)
        assertEquals(5, db.serviceDao().count())
    }

    @Test(timeout = 5_000)
    fun agendar_guarda_cita_con_total_y_adicionales() = runBlocking {
        seeder.seedIfEmpty()
        val servicio = servicioRepositorio.observarServicios().first()
            .first { it.nombre == "Corte de cabello" }

        val resultado = citaRepositorio.agendar(
            Cita(
                id = "",
                cliente = "Cliente Room",
                servicioId = servicio.id,
                tipoAtencion = TipoAtencion.LOCAL,
                adicionales = listOf("Lavado"),
                hora = "15:00",
                fecha = "Hoy",
                estado = EstadoCita.PENDIENTE,
            ),
        )

        assertEquals(ResultadoAgendar.Exito, resultado)

        val detalle = db.appointmentDao()
            .observeWithDetailsByDate(LocalDate.now())
            .first()
            .first { it.appointment.customerName == "Cliente Room" }

        assertEquals(200.0, detalle.appointment.totalPrice, 0.01)
        assertEquals(listOf("Lavado"), detalle.addons.map { it.name })
        assertEquals(EstadoCita.PENDIENTE, detalle.appointment.status)
    }

    @Test(timeout = 5_000)
    fun horario_ocupado_y_cancelar_libera_el_horario() = runBlocking {
        seeder.seedIfEmpty()

        assertTrue(citaRepositorio.horarioOcupado("Hoy", "09:00"))
        assertEquals(
            ResultadoAgendar.HorarioOcupado,
            citaRepositorio.agendar(
                Cita(
                    id = "",
                    cliente = "Otro cliente",
                    servicioId = servicioRepositorio.observarServicios().first().first().id,
                    tipoAtencion = TipoAtencion.LOCAL,
                    hora = "09:00",
                    fecha = "Hoy",
                ),
            ),
        )

        val citaConfirmada = citaRepositorio.observarCitas("Hoy").first()
            .first { it.hora == "09:00" }
        assertTrue(citaRepositorio.cancelar(citaConfirmada.id))
        assertFalse(citaRepositorio.horarioOcupado("Hoy", "09:00"))
    }

    @Test(timeout = 5_000)
    fun confirmar_actualiza_el_estado_de_la_cita() = runBlocking {
        seeder.seedIfEmpty()
        val pendiente = citaRepositorio.observarCitas("Hoy").first()
            .first { it.estado == EstadoCita.PENDIENTE }

        assertTrue(citaRepositorio.confirmar(pendiente.id))

        val actualizada = citaRepositorio.observarCita(pendiente.id).first()
        assertEquals(EstadoCita.CONFIRMADA, actualizada?.estado)
    }

    @Test(timeout = 5_000)
    fun guardar_servicio_y_eliminar_lo_deja_inactivo() = runBlocking {
        seeder.seedIfEmpty()

        val nuevoId = servicioRepositorio.guardarServicio(
            Servicio(
                id = "",
                nombre = "Servicio Room",
                precio = 99.0,
                duracionMin = 20,
            ),
        )
        val nuevo = servicioRepositorio.obtenerServicio(nuevoId)
        assertEquals("Servicio Room", nuevo?.nombre)
        assertTrue(nuevo?.activo == true)

        assertTrue(servicioRepositorio.eliminarServicio(nuevoId))
        assertFalse(servicioRepositorio.observarServicios().first().any { it.id == nuevoId })
        assertFalse(
            servicioRepositorio.observarTodosServicios().first()
                .first { it.id == nuevoId }
                .activo,
        )
    }
}
