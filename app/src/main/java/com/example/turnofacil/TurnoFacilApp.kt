package com.example.turnofacil

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
import com.example.turnofacil.ui.viewmodel.AgendarCitaViewModel
import com.example.turnofacil.ui.viewmodel.CitaRepositorio
import com.example.turnofacil.ui.viewmodel.CitaRepositorioDemo
import com.example.turnofacil.ui.viewmodel.ServicioRepositorio
import com.example.turnofacil.ui.viewmodel.ServicioRepositorioDemo
import com.example.turnofacil.ui.viewmodel.ServiciosViewModel

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

    val servicioRepositorio: ServicioRepositorio = remember { ServicioRepositorioDemo() }
    val citaRepositorio: CitaRepositorio = remember { CitaRepositorioDemo() }

    val serviciosViewModel: ServiciosViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                ServiciosViewModel(servicioRepositorio)
            }
        }
    )

    val navegarPrincipal: (Destino) -> Unit = { destino ->
        val ruta = if (destino is Destino.Agendar) Destino.Agendar.crearRuta() else destino.ruta
        navController.navigate(ruta) {
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
                val serviciosUiState by serviciosViewModel.uiState.collectAsStateWithLifecycle()
                ServiciosScreen(
                    uiState = serviciosUiState,
                    onAgendar = { servicioId ->
                        navController.navigate(Destino.Agendar.crearRuta(servicioId))
                    },
                    onAdminServicios = { navController.navigate(Destino.AdminServicios.ruta) },
                )
            }

            composable(
                route = Destino.Agendar.ruta,
                arguments = listOf(
                    navArgument(Destino.Agendar.ARG_SERVICIO_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                ),
            ) { entry ->
                val servicioId = entry.arguments?.getString(Destino.Agendar.ARG_SERVICIO_ID)
                val agendarCitaViewModel: AgendarCitaViewModel = viewModel(
                    key = "AgendarCitaViewModel_$servicioId",
                    factory = viewModelFactory {
                        initializer {
                            AgendarCitaViewModel(
                                servicioRepositorio = servicioRepositorio,
                                citaRepositorio = citaRepositorio,
                                servicioIdInicial = servicioId,
                            )
                        }
                    }
                )
                val agendarUiState by agendarCitaViewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(agendarUiState.guardadoExitoso) {
                    if (agendarUiState.guardadoExitoso) {
                        navegarPrincipal(Destino.Agenda)
                    }
                }
                AgendarCitaScreen(
                    uiState = agendarUiState,
                    onNombreChange = agendarCitaViewModel::onNombreChange,
                    onServicioChange = agendarCitaViewModel::onServicioChange,
                    onTipoAtencionChange = agendarCitaViewModel::onTipoAtencionChange,
                    onAdicionalToggle = agendarCitaViewModel::onAdicionalToggle,
                    onHoraChange = agendarCitaViewModel::onHoraChange,
                    onConfirmar = agendarCitaViewModel::enviar,
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
