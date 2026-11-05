package com.example.turnofacil.data.demo

import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.EstadoCita
import com.example.turnofacil.data.model.Servicio
import com.example.turnofacil.data.model.TipoAtencion

/**
 * Datos de demostración para bosquejar las pantallas.
 * Más adelante serán reemplazados por el repositorio (Room + API web).
 */
object DatosDemo {

    val servicios: List<Servicio> = listOf(
        Servicio("s1", "Corte de cabello", 150.0, 30),
        Servicio("s2", "Corte + barba", 220.0, 45),
        Servicio("s3", "Diseño de ceja", 60.0, 15),
        Servicio("s4", "Tinte", 380.0, 60),
        Servicio("s5", "Tratamiento capilar", 250.0, 40),
    )

    data class AdicionalDemo(val nombre: String, val precio: Double)

    val adicionales: List<AdicionalDemo> = listOf(
        AdicionalDemo("Lavado", 50.0),
        AdicionalDemo("Diseño de ceja", 60.0),
        AdicionalDemo("Tratamiento", 100.0),
        AdicionalDemo("Barba", 80.0),
    )

    val citas: List<Cita> = listOf(
        Cita(
            id = "c1",
            cliente = "Ana López",
            servicioId = "s1",
            tipoAtencion = TipoAtencion.LOCAL,
            adicionales = listOf("Lavado"),
            hora = "09:00",
            fecha = "Hoy",
            estado = EstadoCita.CONFIRMADA,
            nota = "Cliente frecuente",
        ),
        Cita(
            id = "c2",
            cliente = "Carlos Ramírez",
            servicioId = "s2",
            tipoAtencion = TipoAtencion.LOCAL,
            adicionales = listOf("Diseño de ceja"),
            hora = "10:30",
            fecha = "Hoy",
            estado = EstadoCita.PENDIENTE,
        ),
        Cita(
            id = "c3",
            cliente = "María Torres",
            servicioId = "s4",
            tipoAtencion = TipoAtencion.DOMICILIO,
            adicionales = emptyList(),
            hora = "12:00",
            fecha = "Hoy",
            estado = EstadoCita.PENDIENTE,
            nota = "Domicilio a dos cuadras",
        ),
        Cita(
            id = "c4",
            cliente = "Luis Hernández",
            servicioId = "s5",
            tipoAtencion = TipoAtencion.LOCAL,
            adicionales = listOf("Lavado", "Tratamiento"),
            hora = "16:00",
            fecha = "Hoy",
            estado = EstadoCita.CONFIRMADA,
        ),
    )

    fun servicioPorId(id: String): Servicio? = servicios.firstOrNull { it.id == id }

    fun citaPorId(id: String): Cita? = citas.firstOrNull { it.id == id }

    /** Servicios adicionales que se pueden marcar con Checkbox al agendar. */
    val serviciosAdicionales: List<String>
        get() = adicionales.map { it.nombre }
}
