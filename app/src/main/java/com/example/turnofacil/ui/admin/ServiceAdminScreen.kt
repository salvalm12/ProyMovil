package com.example.turnofacil.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.turnofacil.R
import com.example.turnofacil.data.model.Servicio
import com.example.turnofacil.ui.components.formatearPrecio
import java.util.UUID

/**
 * Pantalla de Administración de Servicios (RF8).
 * Permite la alta, edición y baja lógica (Regla 6) de los servicios ofrecidos por el negocio.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceAdminScreen(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ServiceAdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val strErrorNombre = stringResource(R.string.admin_error_nombre_vacio)
    val strErrorPrecio = stringResource(R.string.admin_error_precio_invalido)
    val strErrorDuracion = stringResource(R.string.admin_error_duracion_invalida)

    LaunchedEffect(uiState.mensajeError) {
        uiState.mensajeError?.let { key ->
            val msg = when (key) {
                "admin_error_nombre_vacio" -> strErrorNombre
                "admin_error_precio_invalido" -> strErrorPrecio
                "admin_error_duracion_invalida" -> strErrorDuracion
                else -> key
            }
            snackbarHostState.showSnackbar(msg)
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.admin_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.accion_volver),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.abrirDialogoCrear() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.admin_nuevo_servicio),
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.admin_subtitulo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(uiState.servicios, key = { it.id }) { servicio ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = servicio.nombre,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                if (!servicio.activo) {
                                    Text(
                                        text = "(${stringResource(R.string.admin_estado_inactivo)})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(
                                    R.string.precio_formato,
                                    formatearPrecio(servicio.precio),
                                ) + " · " + stringResource(
                                    R.string.servicios_duracion,
                                    servicio.duracionMin,
                                ),
                            )
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { viewModel.abrirDialogoEditar(servicio) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_edit),
                                        contentDescription = stringResource(R.string.accion_editar),
                                    )
                                }
                                IconButton(onClick = { viewModel.solicitarEliminar(servicio) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_delete),
                                        contentDescription = stringResource(R.string.accion_eliminar),
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }
    }

    if (uiState.mostrarDialogoServicio) {
        DialogoFormularioServicio(
            servicio = uiState.servicioEditando,
            onDismiss = { viewModel.cerrarDialogoServicio() },
            onGuardar = { servicio -> viewModel.guardarServicio(servicio) },
        )
    }

    if (uiState.mostrarDialogoBajaLogica && uiState.servicioParaBajaLogica != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cerrarDialogoBajaLogica() },
            title = { Text(stringResource(R.string.admin_eliminar_servicio)) },
            text = { Text(stringResource(R.string.admin_baja_logica_mensaje)) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmarBajaLogica() }) {
                    Text(stringResource(R.string.accion_aceptar))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cerrarDialogoBajaLogica() }) {
                    Text(stringResource(R.string.accion_cancelar))
                }
            }
        )
    }
}

@Composable
private fun DialogoFormularioServicio(
    servicio: Servicio?,
    onDismiss: () -> Unit,
    onGuardar: (Servicio) -> Unit,
) {
    var nombre by remember { mutableStateOf(servicio?.nombre ?: "") }
    var precio by remember {
        mutableStateOf(servicio?.let { formatearPrecio(it.precio) } ?: "")
    }
    var duracion by remember {
        mutableStateOf(servicio?.duracionMin?.toString() ?: "30")
    }
    var activo by remember { mutableStateOf(servicio?.activo ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (servicio == null) R.string.admin_nuevo_servicio else R.string.admin_editar_servicio
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text(stringResource(R.string.admin_nombre)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text(stringResource(R.string.admin_precio)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = duracion,
                    onValueChange = { duracion = it },
                    label = { Text(stringResource(R.string.admin_duracion)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(R.string.admin_disponible))
                    Switch(checked = activo, onCheckedChange = { activo = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onGuardar(
                        Servicio(
                            id = servicio?.id ?: UUID.randomUUID().toString(),
                            nombre = nombre.trim(),
                            precio = precio.replace(',', '.').toDoubleOrNull() ?: 0.0,
                            duracionMin = duracion.toIntOrNull() ?: 0,
                            activo = activo,
                        )
                    )
                },
            ) {
                Text(stringResource(R.string.accion_guardar))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.accion_cancelar))
            }
        },
    )
}
