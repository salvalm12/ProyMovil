package com.example.turnofacil.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.turnofacil.R
import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.Servicio
import com.example.turnofacil.ui.components.EtiquetaEstado
import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.ui.agenda.AgendaScreen

@Composable
fun AgendaDelDiaScreen(
    citas: List<Cita>,
    servicios: List<Servicio>,
    onVerDetalle: (String) -> Unit,
    onConfirmar: (String) -> Unit,
    onCancelar: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.agenda_titulo)) }) },
    ) { innerPadding ->
        if (citas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.agenda_vacia))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        text = stringResource(R.string.agenda_subtitulo),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                items(citas, key = { it.id }) { cita ->
                    TarjetaCita(
                        cita = cita,
                        servicio = servicios.firstOrNull { it.id == cita.servicioId },
                        onVerDetalle = onVerDetalle,
                        onConfirmar = onConfirmar,
                        onCancelar = onCancelar,
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaCita(
    cita: Cita,
    servicio: Servicio?,
    onVerDetalle: (String) -> Unit,
    onConfirmar: (String) -> Unit,
    onCancelar: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tipoTexto = stringResource(cita.tipoAtencion.etiqueta)

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
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
            )
            Text(
                text = "${servicio?.nombre ?: cita.servicioId} · $tipoTexto",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { onVerDetalle(cita.id) }) {
                    Text(stringResource(R.string.agenda_ver_detalle))
                }
                TextButton(onClick = { onConfirmar(cita.id) }) {
                    Text(stringResource(R.string.accion_confirmar))
                }
                TextButton(onClick = { onCancelar(cita.id) }) {
                    Text(
                        text = stringResource(R.string.accion_cancelar),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
    AgendaScreen(
        onVerDetalle = onVerDetalle,
        modifier = modifier
    )
}
