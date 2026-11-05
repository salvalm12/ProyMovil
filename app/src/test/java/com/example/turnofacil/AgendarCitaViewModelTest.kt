package com.example.turnofacil

import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.EstadoCita
import com.example.turnofacil.data.model.Servicio
import com.example.turnofacil.data.model.TipoAtencion
import com.example.turnofacil.ui.viewmodel.Adicional
import com.example.turnofacil.ui.viewmodel.AgendarCitaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AgendarCitaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val servicioPrueba = Servicio("s1", "Corte de cabello", 150.0, 30, activo = true)
    private val adicionalLavado = Adicional("Lavado", 50.0)
    private val adicionalMasaje = Adicional("Masaje", 80.0)

    @Test
    fun `total suma el precio del servicio mas los adicionales seleccionados`() {
        val servicioRepo = FakeServicioRepositorio(
            serviciosIniciales = listOf(servicioPrueba),
            adicionalesIniciales = listOf(adicionalLavado, adicionalMasaje),
        )
        val citaRepo = FakeCitaRepositorio()
        val viewModel = AgendarCitaViewModel(servicioRepo, citaRepo)

        // Servicio base = 150.0
        assertEquals(150.0, viewModel.uiState.value.total, 0.01)

        // Agrega lavado (150 + 50 = 200)
        viewModel.onAdicionalToggle(adicionalLavado)
        assertEquals(200.0, viewModel.uiState.value.total, 0.01)

        // Agrega masaje (200 + 80 = 280)
        viewModel.onAdicionalToggle(adicionalMasaje)
        assertEquals(280.0, viewModel.uiState.value.total, 0.01)
    }

    @Test
    fun `nombre vacio marca error`() {
        val servicioRepo = FakeServicioRepositorio(serviciosIniciales = listOf(servicioPrueba))
        val citaRepo = FakeCitaRepositorio()
        val viewModel = AgendarCitaViewModel(servicioRepo, citaRepo)

        viewModel.onNombreChange("")
        viewModel.onHoraChange("10:00")
        viewModel.enviar()

        assertTrue(viewModel.uiState.value.errorNombreVacio)
        assertFalse(viewModel.uiState.value.guardadoExitoso)
    }

    @Test
    fun `hora fuera de horario de atencion marca error`() {
        val servicioRepo = FakeServicioRepositorio(serviciosIniciales = listOf(servicioPrueba))
        val citaRepo = FakeCitaRepositorio()
        val viewModel = AgendarCitaViewModel(servicioRepo, citaRepo)

        viewModel.onNombreChange("Juan Perez")
        viewModel.onHoraChange("08:00") // Horario atencion: 09:00 a 18:00
        viewModel.enviar()

        assertTrue(viewModel.uiState.value.errorHoraFueraHorario)
        assertFalse(viewModel.uiState.value.guardadoExitoso)
    }

    @Test
    fun `horario ocupado marca error`() {
        val citaExistente = Cita(
            id = "c1",
            cliente = "Maria",
            servicioId = "s1",
            tipoAtencion = TipoAtencion.LOCAL,
            hora = "10:00",
            fecha = "Hoy",
            estado = EstadoCita.CONFIRMADA,
        )
        val servicioRepo = FakeServicioRepositorio(serviciosIniciales = listOf(servicioPrueba))
        val citaRepo = FakeCitaRepositorio(citasIniciales = listOf(citaExistente))
        val viewModel = AgendarCitaViewModel(servicioRepo, citaRepo)

        viewModel.onNombreChange("Carlos")
        viewModel.onHoraChange("10:00") // Mismo horario ocupado
        viewModel.enviar()

        assertTrue(viewModel.uiState.value.errorHorarioOcupado)
        assertFalse(viewModel.uiState.value.guardadoExitoso)
    }

    @Test
    fun `una cita cancelada libera el horario`() {
        val citaCancelada = Cita(
            id = "c1",
            cliente = "Maria",
            servicioId = "s1",
            tipoAtencion = TipoAtencion.LOCAL,
            hora = "10:00",
            fecha = "Hoy",
            estado = EstadoCita.CANCELADA,
        )
        val servicioRepo = FakeServicioRepositorio(serviciosIniciales = listOf(servicioPrueba))
        val citaRepo = FakeCitaRepositorio(citasIniciales = listOf(citaCancelada))
        val viewModel = AgendarCitaViewModel(servicioRepo, citaRepo)

        viewModel.onNombreChange("Carlos")
        viewModel.onHoraChange("10:00") // Cita anterior cancelada, horario libre
        viewModel.enviar()

        assertFalse(viewModel.uiState.value.errorHorarioOcupado)
        assertTrue(viewModel.uiState.value.guardadoExitoso)
    }

    @Test
    fun `una cita nueva queda PENDIENTE`() {
        val servicioRepo = FakeServicioRepositorio(serviciosIniciales = listOf(servicioPrueba))
        val citaRepo = FakeCitaRepositorio()
        val viewModel = AgendarCitaViewModel(servicioRepo, citaRepo)

        viewModel.onNombreChange("Carlos")
        viewModel.onHoraChange("11:00")
        viewModel.enviar()

        assertTrue(viewModel.uiState.value.guardadoExitoso)
        assertEquals(1, citaRepo.citas.size)
        assertEquals(EstadoCita.PENDIENTE, citaRepo.citas.last().estado)
    }
}
