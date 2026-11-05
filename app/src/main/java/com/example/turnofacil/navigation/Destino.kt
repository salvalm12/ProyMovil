package com.example.turnofacil.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.turnofacil.R

/** Rutas de navegación de la aplicación. */
sealed class Destino(val ruta: String) {
    data object Inicio : Destino("inicio")
    data object Servicios : Destino("servicios")
    data object Agendar : Destino("agendar?servicioId={servicioId}") {
        const val ARG_SERVICIO_ID = "servicioId"
        fun crearRuta(servicioId: String? = null): String =
            if (servicioId.isNullOrEmpty()) "agendar" else "agendar?servicioId=$servicioId"
    }
    data object Agenda : Destino("agenda")
    data object AdminServicios : Destino("admin_servicios")
    data object Configuracion : Destino("configuracion")

    data object DetalleCita : Destino("detalle_cita/{citaId}") {
        const val ARG_CITA_ID = "citaId"
        fun crearRuta(citaId: String): String = "detalle_cita/$citaId"
    }
}

/**
 * Destinos de primer nivel que aparecen en el menú adaptable
 * (NavigationBar / NavigationRail / NavigationDrawer).
 */
enum class DestinoPrincipal(
    val destino: Destino,
    @param:StringRes val etiqueta: Int,
    @param:DrawableRes val icono: Int,
) {
    INICIO(Destino.Inicio, R.string.nav_inicio, R.drawable.ic_home),
    SERVICIOS(Destino.Servicios, R.string.nav_servicios, R.drawable.ic_storefront),
    AGENDAR(Destino.Agendar, R.string.nav_agendar, R.drawable.ic_add_circle),
    AGENDA(Destino.Agenda, R.string.nav_agenda, R.drawable.ic_calendar_today),
    CONFIGURACION(Destino.Configuracion, R.string.nav_configuracion, R.drawable.ic_settings),
}
