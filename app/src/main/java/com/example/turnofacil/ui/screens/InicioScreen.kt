package com.example.turnofacil.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.turnofacil.R
import com.example.turnofacil.data.demo.DatosDemo
import com.example.turnofacil.ui.components.BotonAccion
import com.example.turnofacil.ui.components.EstadoConexion
import com.example.turnofacil.ui.components.TarjetaResumen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    onVerServicios: () -> Unit,
    onAgendar: () -> Unit,
    onVerAgenda: () -> Unit,
    onAdminServicios: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.inicio_titulo)) }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.inicio_bienvenida),
                style = MaterialTheme.typography.headlineSmall,
            )

            Text(
                text = stringResource(R.string.inicio_resumen_dia),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TarjetaResumen(
                    valor = DatosDemo.citas.size.toString(),
                    etiqueta = stringResource(R.string.inicio_citas_hoy),
                    icono = R.drawable.ic_event,
                    modifier = Modifier.weight(1f),
                )
                TarjetaResumen(
                    valor = DatosDemo.servicios.size.toString(),
                    etiqueta = stringResource(R.string.inicio_servicios_activos),
                    icono = R.drawable.ic_list,
                    modifier = Modifier.weight(1f),
                )
            }

            Text(
                text = stringResource(R.string.inicio_acciones_rapidas),
                style = MaterialTheme.typography.titleMedium,
            )
            BotonAccion(
                texto = stringResource(R.string.inicio_agendar_cita),
                icono = R.drawable.ic_add_circle,
                onClick = onAgendar,
            )
            BotonAccion(
                texto = stringResource(R.string.inicio_ver_agenda),
                icono = R.drawable.ic_calendar_today,
                onClick = onVerAgenda,
            )
            BotonAccion(
                texto = stringResource(R.string.nav_servicios),
                icono = R.drawable.ic_storefront,
                onClick = onVerServicios,
            )
            BotonAccion(
                texto = stringResource(R.string.inicio_admin_servicios),
                icono = R.drawable.ic_edit,
                onClick = onAdminServicios,
            )

            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.inicio_estado_sincronizacion),
                style = MaterialTheme.typography.titleMedium,
            )
            EstadoConexion(conectado = true)
        }
    }
}
