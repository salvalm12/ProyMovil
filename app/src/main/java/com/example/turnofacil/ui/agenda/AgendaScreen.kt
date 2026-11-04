package com.example.turnofacil.ui.agenda

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.turnofacil.R
import com.example.turnofacil.data.demo.DatosDemo
import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.EstadoCita
import com.example.turnofacil.ui.components.EtiquetaEstado

/**
 * Pantalla de Agenda del Día (RF4).
 * Muestra las citas del día ordenadas por hora, con chips de estado,
 * filtro por estado (Pendiente / Confirmada / Todos) y un indicador de sincronización.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    onVerDetalle: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AgendaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.agenda_titulo)) }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Indicador de Sincronización (RF4 / RF9)
            IndicadorSincronizacion(estaSincronizado = uiState.estaSincronizado)

            // Filtros de Estado (Pendiente, Confirmada, Cancelada, Todos)
            FilaFiltrosEstado(
                estadoSeleccionado = uiState.estadoFiltro,
                onSeleccionarEstado = { viewModel.filtrarPorEstado(it) }
            )

            if (uiState.citas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.agenda_vacia),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.agenda_subtitulo),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(uiState.citas, key = { it.id }) { cita ->
                        TarjetaCitaAgenda(
                            cita = cita,
                            onVerDetalle = onVerDetalle,
                            onConfirmar = { viewModel.confirmarCita(cita.id) },
                            onCancelar = { viewModel.cancelarCita(cita.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IndicadorSincronizacion(
    estaSincronizado: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = if (estaSincronizado) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }
    val contentColor = if (estaSincronizado) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(
                    if (estaSincronizado) R.string.agenda_sync_conectado else R.string.agenda_sync_pendiente
                ),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilaFiltrosEstado(
    estadoSeleccionado: EstadoCita?,
    onSeleccionarEstado: (EstadoCita?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = estadoSeleccionado == null,
            onClick = { onSeleccionarEstado(null) },
            label = { Text(stringResource(R.string.agenda_filtro_todos)) }
        )
        FilterChip(
            selected = estadoSeleccionado == EstadoCita.PENDIENTE,
            onClick = { onSeleccionarEstado(EstadoCita.PENDIENTE) },
            label = { Text(stringResource(R.string.agenda_filtro_pendiente)) }
        )
        FilterChip(
            selected = estadoSeleccionado == EstadoCita.CONFIRMADA,
            onClick = { onSeleccionarEstado(EstadoCita.CONFIRMADA) },
            label = { Text(stringResource(R.string.agenda_filtro_confirmada)) }
        )
        FilterChip(
            selected = estadoSeleccionado == EstadoCita.CANCELADA,
            onClick = { onSeleccionarEstado(EstadoCita.CANCELADA) },
            label = { Text(stringResource(R.string.agenda_filtro_cancelada)) }
        )
    }
}

@Composable
private fun TarjetaCitaAgenda(
    cita: Cita,
    onVerDetalle: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val servicio = DatosDemo.servicioPorId(cita.servicioId)
    val tipoTexto = stringResource(cita.tipoAtencion.etiqueta)

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = cita.hora,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                EtiquetaEstado(estado = cita.estado)
            }
            Text(
                text = cita.cliente,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${servicio?.nombre ?: cita.servicioId} · $tipoTexto",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onVerDetalle(cita.id) }) {
                    Text(stringResource(R.string.agenda_ver_detalle))
                }
                if (cita.estado == EstadoCita.PENDIENTE) {
                    TextButton(onClick = onConfirmar) {
                        Text(stringResource(R.string.agenda_accion_confirmar))
                    }
                }
                if (cita.estado != EstadoCita.CANCELADA) {
                    TextButton(onClick = onCancelar) {
                        Text(
                            text = stringResource(R.string.agenda_accion_cancelar),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}
