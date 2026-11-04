package com.example.turnofacil.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.turnofacil.ui.admin.ServiceAdminScreen

@Composable
fun AdminServiciosScreen(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ServiceAdminScreen(
        onVolver = onVolver,
        modifier = modifier
    )
}
