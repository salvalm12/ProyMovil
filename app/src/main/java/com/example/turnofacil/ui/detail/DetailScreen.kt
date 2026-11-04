package com.example.turnofacil.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.example.turnofacil.R
import com.example.turnofacil.data.model.EstadoCita
import com.example.turnofacil.ui.components.EtiquetaEstado
import com.example.turnofacil.ui.components.FilaDetalle
import com.example.turnofacil.ui.components.formatearPrecio

/**
 * Pantalla de Detalle de Cita (RF5).
 * Muestra información completa del turno, calcula el precio total (Regla 4)
 * y ofrece las acciones Confirmar, Reprogramar y Cancelar (Regla 1 y 3).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onVolver: () -> Unit,
    onReprogramarExterna: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val strErrorHorarioOcupado = stringResource(R.string.detail_error_horario_ocupado)
    val strExitoConfirmada = stringResource(R.string.detail_exito_confirmada)
    val strExitoCancelada = stringResource(R.string.detail_exito_cancelada)
    val strExitoReprogramada = stringResource(R.string.detail_exito_reprogramada)

    LaunchedEffect(uiState.error, uiState.mensajeExito) {
        uiState.error?.let { err ->
            val msg = if (err.contains("ocupado", ignoreCase = true)) strErrorHorarioOcupado else err
            snackbarHostState.showSnackbar(msg)
            viewModel.limpiarMensajes()
        }
        uiState.mensajeExito?.let { key ->
            val msg = when (key) {
                "detail_exito_confirmada" -> strExitoConfirmada
                "detail_exito_cancelada" -> strExitoCancelada
                "detail_exito_reprogramada" -> strExitoReprogramada
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
                title = { Text(stringResource(R.string.detail_titulo)) },
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
    ) { innerPadding ->
        val cita = uiState.cita
        if (cita == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.detail_no_encontrada),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            val servicio = uiState.servicio
            val adicionalesTexto = if (cita.adicionales.isEmpty()) "—" else cita.adicionales.joinToString(", ")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Tarjeta de Precios y Estado
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.detail_precio_total),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = stringResource(
                                        R.string.precio_formato,
                                        formatearPrecio(uiState.precioTotal),
                                    ),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            EtiquetaEstado(estado = cita.estado)
                        }

                        HorizontalDivider()

                        // Desglose de precios (Regla de negocio 4)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(R.string.detail_precio_base),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = stringResource(R.string.precio_formato, formatearPrecio(uiState.precioBase)),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        if (uiState.precioAdicionales > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = stringResource(R.string.detail_precio_adicionales),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                Text(
                                    text = stringResource(R.string.precio_formato, formatearPrecio(uiState.precioAdicionales)),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }

                        HorizontalDivider()

                        // Detalles de la cita
                        FilaDetalle(
                            etiqueta = stringResource(R.string.detail_cliente),
                            valor = cita.cliente,
                        )
                        FilaDetalle(
                            etiqueta = stringResource(R.string.detail_servicio),
                            valor = servicio?.nombre ?: cita.servicioId,
                        )
                        FilaDetalle(
                            etiqueta = stringResource(R.string.detail_tipo_atencion),
                            valor = stringResource(cita.tipoAtencion.etiqueta),
                        )
                        FilaDetalle(
                            etiqueta = stringResource(R.string.detail_adicionales),
                            valor = adicionalesTexto,
                        )
                        FilaDetalle(
                            etiqueta = stringResource(R.string.detail_fecha_hora),
                            valor = "${cita.fecha} · ${cita.hora}",
                        )
                        FilaDetalle(
                            etiqueta = stringResource(R.string.detail_estado),
                            valor = stringResource(cita.estado.etiqueta),
                        )
                        if (cita.nota.isNotBlank()) {
                            FilaDetalle(
                                etiqueta = stringResource(R.string.detail_nota),
                                valor = cita.nota,
                            )
                        }
                    }
                }

                // Botones de acción (RF5)
                if (cita.estado == EstadoCita.PENDIENTE) {
                    Button(
                        onClick = { viewModel.confirmarCita() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.detail_confirmar))
                    }
                }

                if (cita.estado != EstadoCita.CANCELADA) {
                    OutlinedButton(
                        onClick = {
                            if (onReprogramarExterna != null) {
                                onReprogramarExterna()
                            } else {
                                viewModel.abrirDialogoReprogramar()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.detail_reprogramar))
                    }

                    OutlinedButton(
                        onClick = { viewModel.cancelarCita() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(R.string.detail_cancelar),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (uiState.mostrarDialogoReprogramar && uiState.cita != null) {
        DialogoReprogramarCita(
            fechaActual = uiState.cita?.fecha ?: "Hoy",
            horaActual = uiState.cita?.hora ?: "10:00",
            onDismiss = { viewModel.cerrarDialogoReprogramar() },
            onGuardar = { nuevaFecha, nuevaHora ->
                viewModel.reprogramarCita(nuevaFecha, nuevaHora)
            },
        )
    }
}

@Composable
private fun DialogoReprogramarCita(
    fechaActual: String,
    horaActual: String,
    onDismiss: () -> Unit,
    onGuardar: (String, String) -> Unit,
) {
    var fecha by remember { mutableStateOf(fechaActual) }
    var hora by remember { mutableStateOf(horaActual) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.detail_dialogo_reprogramar_titulo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text(stringResource(R.string.detail_nueva_fecha)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = hora,
                    onValueChange = { hora = it },
                    label = { Text(stringResource(R.string.detail_nueva_hora)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (fecha.isNotBlank() && hora.isNotBlank()) {
                        onGuardar(fecha.trim(), hora.trim())
                    }
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
