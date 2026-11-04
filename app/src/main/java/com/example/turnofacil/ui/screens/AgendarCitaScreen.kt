package com.example.turnofacil.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.turnofacil.R
import com.example.turnofacil.data.model.Servicio
import com.example.turnofacil.data.model.TipoAtencion
import com.example.turnofacil.ui.components.formatearPrecio
import com.example.turnofacil.ui.viewmodel.Adicional
import com.example.turnofacil.ui.viewmodel.AgendarCitaUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendarCitaScreen(
    uiState: AgendarCitaUiState,
    onNombreChange: (String) -> Unit,
    onServicioChange: (Servicio) -> Unit,
    onTipoAtencionChange: (TipoAtencion) -> Unit,
    onAdicionalToggle: (Adicional) -> Unit,
    onHoraChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuServicios by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.agendar_titulo)) }) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (uiState.cargando) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 840.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Nombre del cliente
                    OutlinedTextField(
                        value = uiState.nombreCliente,
                        onValueChange = onNombreChange,
                        label = { Text(stringResource(R.string.agendar_nombre)) },
                        isError = uiState.errorNombreVacio,
                        supportingText = {
                            if (uiState.errorNombreVacio) {
                                Text(
                                    text = stringResource(R.string.agendar_error_nombre),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Selección de servicio
                    Text(
                        text = stringResource(R.string.agendar_servicio),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { menuServicios = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = uiState.servicioSeleccionado?.let {
                                    "${it.nombre} — " + stringResource(
                                        R.string.precio_formato,
                                        formatearPrecio(it.precio),
                                    )
                                } ?: stringResource(R.string.agendar_seleccionar_servicio),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        DropdownMenu(
                            expanded = menuServicios,
                            onDismissRequest = { menuServicios = false },
                        ) {
                            uiState.serviciosDisponibles.forEach { servicio ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${servicio.nombre} — " + stringResource(
                                                R.string.precio_formato,
                                                formatearPrecio(servicio.precio),
                                            ),
                                        )
                                    },
                                    onClick = {
                                        onServicioChange(servicio)
                                        menuServicios = false
                                    },
                                )
                            }
                        }
                    }

                    // Tipo de atención (RadioButton)
                    Text(
                        text = stringResource(R.string.agendar_tipo_atencion),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Column {
                        FilaRadio(
                            texto = stringResource(R.string.agendar_tipo_local),
                            seleccionado = uiState.tipoAtencion == TipoAtencion.LOCAL,
                            onClick = { onTipoAtencionChange(TipoAtencion.LOCAL) },
                        )
                        FilaRadio(
                            texto = stringResource(R.string.agendar_tipo_domicilio),
                            seleccionado = uiState.tipoAtencion == TipoAtencion.DOMICILIO,
                            onClick = { onTipoAtencionChange(TipoAtencion.DOMICILIO) },
                        )
                    }

                    // Servicios adicionales (Checkbox)
                    if (uiState.adicionalesDisponibles.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.agendar_adicionales),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Column {
                            uiState.adicionalesDisponibles.forEach { adicional ->
                                val estaMarcado = uiState.adicionalesSeleccionados.contains(adicional)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onAdicionalToggle(adicional) },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = estaMarcado,
                                        onCheckedChange = { onAdicionalToggle(adicional) },
                                    )
                                    Text(
                                        text = "${adicional.nombre} (+" + stringResource(
                                            R.string.precio_formato,
                                            formatearPrecio(adicional.precio),
                                        ) + ")",
                                    )
                                }
                            }
                        }
                    }

                    // Hora (TimePicker)
                    Text(
                        text = stringResource(R.string.agendar_hora),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    SelectorHora(
                        hora = uiState.hora,
                        onHoraSeleccionada = onHoraChange,
                    )
                    Text(
                        text = stringResource(
                            R.string.agendar_horario_atencion,
                            uiState.horaApertura,
                            uiState.horaCierre,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (uiState.errorHoraFueraHorario) {
                        Text(
                            text = stringResource(R.string.agendar_error_fuera_horario),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    if (uiState.errorHorarioOcupado) {
                        Text(
                            text = stringResource(R.string.agendar_error_horario_ocupado),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    // Total en vivo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.agendar_total),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(
                                R.string.precio_formato,
                                formatearPrecio(uiState.total),
                            ),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onConfirmar,
                        enabled = !uiState.guardando,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (uiState.guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(stringResource(R.string.agendar_confirmar))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaRadio(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = seleccionado,
                onClick = onClick,
                role = Role.RadioButton,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = seleccionado, onClick = onClick)
        Text(texto)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorHora(
    hora: String,
    onHoraSeleccionada: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    val estadoHora = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = false)

    OutlinedButton(
        onClick = { mostrarDialogo = true },
        modifier = modifier.fillMaxWidth(),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_schedule),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(hora.ifEmpty { stringResource(R.string.agendar_seleccionar_hora) })
    }

    if (mostrarDialogo) {
        Dialog(onDismissRequest = { mostrarDialogo = false }) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.agendar_hora),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    TimePicker(state = estadoHora)
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TextButton(onClick = { mostrarDialogo = false }) {
                            Text(stringResource(R.string.accion_cancelar))
                        }
                        TextButton(
                            onClick = {
                                onHoraSeleccionada(
                                    String.format("%02d:%02d", estadoHora.hour, estadoHora.minute)
                                )
                                mostrarDialogo = false
                            },
                        ) {
                            Text(stringResource(R.string.accion_aceptar))
                        }
                    }
                }
            }
        }
    }
}
