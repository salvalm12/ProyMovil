package com.example.turnofacil.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.turnofacil.R
import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.Servicio
import com.example.turnofacil.ui.components.EtiquetaEstado
import com.example.turnofacil.ui.components.FilaDetalle
import com.example.turnofacil.ui.components.formatearPrecio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleCitaScreen(
    cita: Cita?,
    servicio: Servicio?,
    onVolver: () -> Unit,
    onConfirmar: () -> Unit,
    onReprogramar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detalle_titulo)) },
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
        if (cita == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.detalle_no_encontrada))
            }
        } else {
            val adicionales = if (cita.adicionales.isEmpty()) {
                "—"
            } else {
                cita.adicionales.joinToString(", ")
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
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
                            Text(
                                text = stringResource(
                                    R.string.precio_formato,
                                    formatearPrecio(servicio?.precio ?: 0.0),
                                ),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            EtiquetaEstado(estado = cita.estado)
                        }
                        HorizontalDivider()
                        FilaDetalle(
                            etiqueta = stringResource(R.string.detalle_cliente),
                            valor = cita.cliente,
                        )
                        FilaDetalle(
                            etiqueta = stringResource(R.string.detalle_servicio),
                            valor = servicio?.nombre ?: cita.servicioId,
                        )
                        FilaDetalle(
                            etiqueta = stringResource(R.string.detalle_tipo_atencion),
                            valor = stringResource(cita.tipoAtencion.etiqueta),
                        )
                        FilaDetalle(
                            etiqueta = stringResource(R.string.detalle_adicionales),
                            valor = adicionales,
                        )
                        FilaDetalle(
                            etiqueta = stringResource(R.string.detalle_hora),
                            valor = "${cita.fecha} · ${cita.hora}",
                        )
                        FilaDetalle(
                            etiqueta = stringResource(R.string.detalle_estado),
                            valor = stringResource(cita.estado.etiqueta),
                        )
                        if (cita.nota.isNotBlank()) {
                            FilaDetalle(
                                etiqueta = stringResource(R.string.detalle_nota),
                                valor = cita.nota,
                            )
                        }
                    }
                }

                Button(onClick = onConfirmar, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.accion_confirmar))
                }
                OutlinedButton(onClick = onReprogramar, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.detalle_reprogramar))
                }
                OutlinedButton(onClick = onCancelar, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.accion_cancelar),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
