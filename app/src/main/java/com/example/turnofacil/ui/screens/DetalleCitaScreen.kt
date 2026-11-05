package com.example.turnofacil.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.ui.detail.DetailScreen
import com.example.turnofacil.ui.detail.DetailViewModel

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
    val citaId = cita?.id ?: ""
    val viewModel = remember(citaId) { DetailViewModel(citaId = citaId) }

    DetailScreen(
        viewModel = viewModel,
        onVolver = onVolver,
        onReprogramarExterna = onReprogramar,
        modifier = modifier,
    )
}
