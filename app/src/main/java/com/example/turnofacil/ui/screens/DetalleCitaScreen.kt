package com.example.turnofacil.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.ui.detail.DetailScreen
import com.example.turnofacil.ui.detail.DetailViewModel

@Composable
fun DetalleCitaScreen(
    cita: Cita?,
    onVolver: () -> Unit,
    onConfirmar: () -> Unit,
    onReprogramar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val citaId = cita?.id ?: ""
    val viewModel = remember(citaId) { DetailViewModel(citaId = citaId) }

    DetailScreen(
        viewModel = viewModel,
        onVolver = onVolver,
        onReprogramarExterna = onReprogramar,
        modifier = modifier,
    )
}
