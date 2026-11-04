package com.example.turnofacil

import com.example.turnofacil.data.model.Servicio
import com.example.turnofacil.ui.viewmodel.ServiciosViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class ServiciosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun serviciosInactivosNoAparecenEnLaLista() {
        val servicioActivo = Servicio("s1", "Corte", 100.0, 30, activo = true)
        val servicioInactivo = Servicio("s2", "Tinte Inactivo", 200.0, 60, activo = false)

        val repo = FakeServicioRepositorio(
            serviciosIniciales = listOf(servicioActivo, servicioInactivo),
        )
        val viewModel = ServiciosViewModel(repo)

        val state = viewModel.uiState.value

        assertFalse(state.cargando)
        assertEquals(1, state.servicios.size)
        assertEquals("s1", state.servicios.first().id)
    }
}
