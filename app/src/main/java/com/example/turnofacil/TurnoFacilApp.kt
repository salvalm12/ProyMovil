package com.example.turnofacil

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.turnofacil.data.demo.DatosDemo
import com.example.turnofacil.navigation.Destino
import com.example.turnofacil.navigation.DestinoPrincipal
import com.example.turnofacil.ui.admin.ServiceAdminScreen
import com.example.turnofacil.ui.agenda.AgendaScreen
import com.example.turnofacil.ui.detail.DetailScreen
import com.example.turnofacil.ui.detail.DetailViewModel
import com.example.turnofacil.ui.screens.AgendarCitaScreen
import com.example.turnofacil.ui.screens.InicioScreen
import com.example.turnofacil.ui.screens.ServiciosScreen
import com.example.turnofacil.ui.settings.SettingsScreen
import com.example.turnofacil.ui.theme.TurnoFacilTheme

/**
 * Contenedor principal de la aplicación. Define el menú de navegación adaptable
 * (NavigationBar, NavigationRail o NavigationDrawer según el tamaño de pantalla)
 * y el grafo de navegación entre todas las pantallas.
 */
@PreviewScreenSizes
@Composable
fun TurnoFacilApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destinoActual = backStackEntry?.destination

    val navegarPrincipal: (Destino) -> Unit = { destino ->
        navController.navigate(destino.ruta) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            DestinoPrincipal.entries.forEach { principal ->
                item(
                    selected = destinoActual?.hierarchy?.any {
                        it.route == principal.destino.ruta
                    } == true,
                    onClick = { navegarPrincipal(principal.destino) },
                    icon = {
                        Icon(
                            painter = painterResource(principal.icono),
                            contentDescription = null,
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(principal.etiqueta),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = Destino.Inicio.ruta,
        ) {
            composable(Destino.Inicio.ruta) {
                InicioScreen(
                    onVerServicios = { navegarPrincipal(Destino.Servicios) },
                    onAgendar = { navegarPrincipal(Destino.Agendar) },
                    onVerAgenda = { navegarPrincipal(Destino.Agenda) },
                    onAdminServicios = { navController.navigate(Destino.AdminServicios.ruta) },
                )
            }

            composable(Destino.Servicios.ruta) {
                ServiciosScreen(
                    servicios = DatosDemo.servicios,
                    onAgendar = { navegarPrincipal(Destino.Agendar) },
                    onAdminServicios = { navController.navigate(Destino.AdminServicios.ruta) },
                )
            }

            composable(Destino.Agendar.ruta) {
                AgendarCitaScreen(
                    servicios = DatosDemo.servicios,
                    onConfirmar = { navegarPrincipal(Destino.Agenda) },
                )
            }

            composable(Destino.Agenda.ruta) {
                AgendaScreen(
                    onVerDetalle = { citaId ->
                        navController.navigate(Destino.DetalleCita.crearRuta(citaId))
                    },
                )
            }

            composable(
                route = Destino.DetalleCita.ruta,
                arguments = listOf(
                    navArgument(Destino.DetalleCita.ARG_CITA_ID) { type = NavType.StringType },
                ),
            ) { entry ->
                val citaId = entry.arguments?.getString(Destino.DetalleCita.ARG_CITA_ID) ?: ""
                val viewModel = remember(citaId) { DetailViewModel(citaId = citaId) }
                DetailScreen(
                    viewModel = viewModel,
                    onVolver = { navController.popBackStack() },
                    onReprogramarExterna = { navController.navigate(Destino.Agendar.ruta) },
                )
            }

            composable(Destino.AdminServicios.ruta) {
                ServiceAdminScreen(onVolver = { navController.popBackStack() })
            }

            composable(Destino.Configuracion.ruta) {
                SettingsScreen()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
private fun TurnoFacilAppPreview() {
    TurnoFacilTheme {
        TurnoFacilApp()
    }
}
