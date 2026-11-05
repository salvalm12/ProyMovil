package com.example.turnofacil.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.turnofacil.data.model.Cita
import com.example.turnofacil.ui.agenda.AgendaScreen

@Composable
fun AgendaDelDiaScreen(
    citas: List<Cita>,
    onVerDetalle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AgendaScreen(
        onVerDetalle = onVerDetalle,
        modifier = modifier
    )
}
