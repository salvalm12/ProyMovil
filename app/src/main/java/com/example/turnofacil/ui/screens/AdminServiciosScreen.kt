package com.example.turnofacil.ui.screens

import androidx.compose.runtime.Composable
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
import com.example.turnofacil.R
import com.example.turnofacil.data.model.Servicio
import com.example.turnofacil.ui.components.formatearPrecio
import java.util.UUID
import androidx.compose.ui.Modifier
import com.example.turnofacil.ui.admin.ServiceAdminScreen

@Composable
fun AdminServiciosScreen(
    servicios: List<Servicio>,
    onGuardar: (Servicio) -> Unit,
    onEliminar: (Servicio) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    var servicioEditando by remember { mutableStateOf<Servicio?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
            FloatingActionButton(
                onClick = {
                    servicioEditando = null
                    mostrarDialogo = true
                },
            ) {
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
                )
            }
            items(servicios, key = { it.id }) { servicio ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = servicio.nombre,
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                        supportingContent = {
                            val estado = if (servicio.activo) {
                                ""
                            } else {
                                " · " + stringResource(R.string.admin_inactivo)
                            }
                            Text(
                                text = stringResource(
                                    R.string.precio_formato,
                                    formatearPrecio(servicio.precio),
                                ) + " · " + stringResource(
                                    R.string.servicios_duracion,
                                    servicio.duracionMin,
                                ) + estado,
                            )
                        },
                        trailingContent = {
                            Row {
                                IconButton(
                                    onClick = {
                                        servicioEditando = servicio
                                        mostrarDialogo = true
                                    },
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_edit),
                                        contentDescription = stringResource(R.string.accion_editar),
                                    )
                                }
                                IconButton(onClick = { onEliminar(servicio) }) {
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

    if (mostrarDialogo) {
        DialogoServicio(
            servicio = servicioEditando,
            onDismiss = { mostrarDialogo = false },
            onGuardar = { servicio ->
                onGuardar(servicio)
                mostrarDialogo = false
            },
        )
    }
}

@Composable
private fun DialogoServicio(
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
                            nombre = nombre.ifBlank { "—" },
                            precio = precio.toDoubleOrNull() ?: 0.0,
                            duracionMin = duracion.toIntOrNull() ?: 30,
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
    ServiceAdminScreen(
        onVolver = onVolver,
        modifier = modifier
    )
}
