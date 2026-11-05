package com.example.turnofacil.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.turnofacil.R
import com.example.turnofacil.data.prefs.PreferenciasApp

/**
 * Pantalla de Configuración (RF9 y RNF3).
 * Incluye selector de idioma, horario de atención y estado de sincronización.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.cargarPreferencias(context)
    }

    val strHorarioGuardado = stringResource(R.string.settings_horario_guardado)
    LaunchedEffect(uiState.mensajeExito) {
        uiState.mensajeExito?.let { key ->
            val msg = if (key == "settings_horario_guardado") strHorarioGuardado else key
            snackbarHostState.showSnackbar(msg)
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_titulo)) }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Idioma (RNF3)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.settings_idioma),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    FilaIdiomaSettings(
                        texto = stringResource(R.string.settings_idioma_sistema),
                        seleccionado = uiState.idioma == PreferenciasApp.IDIOMA_SISTEMA,
                        onClick = { viewModel.cambiarIdioma(context, PreferenciasApp.IDIOMA_SISTEMA) },
                    )
                    FilaIdiomaSettings(
                        texto = stringResource(R.string.settings_idioma_es),
                        seleccionado = uiState.idioma == PreferenciasApp.IDIOMA_ES,
                        onClick = { viewModel.cambiarIdioma(context, PreferenciasApp.IDIOMA_ES) },
                    )
                    FilaIdiomaSettings(
                        texto = stringResource(R.string.settings_idioma_en),
                        seleccionado = uiState.idioma == PreferenciasApp.IDIOMA_EN,
                        onClick = { viewModel.cambiarIdioma(context, PreferenciasApp.IDIOMA_EN) },
                    )
                }
            }

            // Horario de atención (RF9 / Regla 5)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_horario),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    CampoHoraSettings(
                        etiqueta = stringResource(R.string.settings_horario_apertura),
                        valor = uiState.horaApertura,
                        onValor = { viewModel.actualizarHoraApertura(it) },
                    )
                    CampoHoraSettings(
                        etiqueta = stringResource(R.string.settings_horario_cierre),
                        valor = uiState.horaCierre,
                        onValor = { viewModel.actualizarHoraCierre(it) },
                    )
                }
            }

            // Sincronización (RF9)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.settings_sync_activada),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Switch(
                            checked = uiState.syncAutomatica,
                            onCheckedChange = { viewModel.toggleSyncAutomatica(it) },
                        )
                    }
                    Text(
                        text = stringResource(R.string.settings_sync_descripcion),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider()
                    Text(
                        text = "${stringResource(R.string.settings_ultima_sincronizacion)} ${uiState.ultimaSincronizacion}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaIdiomaSettings(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = seleccionado,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = seleccionado, onClick = null)
        Spacer(Modifier.width(8.dp))
        Text(texto)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampoHoraSettings(
    etiqueta: String,
    valor: String,
    onValor: (String) -> Unit,
) {
    var mostrarDialogo by remember { mutableStateOf(false) }

    val horaMin = valor.split(":")
    val horaInicial = horaMin.getOrNull(0)?.toIntOrNull() ?: 9
    val minInicial = horaMin.getOrNull(1)?.toIntOrNull() ?: 0

    val estadoHora = rememberTimePickerState(
        initialHour = horaInicial,
        initialMinute = minInicial,
        is24Hour = true
    )

    OutlinedButton(onClick = { mostrarDialogo = true }, modifier = Modifier.fillMaxWidth()) {
        Icon(
            painter = painterResource(R.drawable.ic_schedule),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text("$etiqueta: $valor", modifier = Modifier.weight(1f))
    }

    if (mostrarDialogo) {
        Dialog(onDismissRequest = { mostrarDialogo = false }) {
            Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(text = etiqueta, style = MaterialTheme.typography.labelMedium)
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
                                onValor(String.format("%02d:%02d", estadoHora.hour, estadoHora.minute))
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
