package com.example.turnofacil.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.data.model.Servicio
import com.example.turnofacil.ui.detail.DetailScreen
import com.example.turnofacil.ui.detail.DetailViewModel

@Composable
fun DetalleCitaScreen(
    cita: Cita? = null,
    servicio: Servicio? = null,
    onVolver: () -> Unit = {},
    onConfirmar: () -> Unit = {},
    onReprogramar: () -> Unit = {},
    onCancelar: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (cita != null) {
        val viewModel = DetailViewModel(citaId = cita.id)
        DetailScreen(
            viewModel = viewModel,
            onVolver = onVolver,
            onReprogramarExterna = onReprogramar,
            modifier = modifier,
        )
    }
}
