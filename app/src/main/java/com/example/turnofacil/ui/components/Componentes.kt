package com.example.turnofacil.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.turnofacil.R
import com.example.turnofacil.data.model.EstadoCita
import java.util.Locale

/** Formatea un precio con dos decimales. */
fun formatearPrecio(precio: Double): String =
    String.format(Locale.getDefault(), "%.2f", precio)

/** Tarjeta con un valor grande y una etiqueta, usada en el resumen del inicio. */
@Composable
fun TarjetaResumen(
    valor: String,
    etiqueta: String,
    @DrawableRes icono: Int,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                painter = painterResource(icono),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(valor, style = MaterialTheme.typography.headlineMedium)
            Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Botón ancho con icono y texto, usado para las acciones rápidas. */
@Composable
fun BotonAccion(
    texto: String,
    @DrawableRes icono: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Icon(
            painter = painterResource(icono),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(texto)
    }
}

/** Chip que muestra el estado de una cita con un color según su valor. */
@Composable
fun EtiquetaEstado(estado: EstadoCita, modifier: Modifier = Modifier) {
    val container: Color
    val content: Color
    when (estado) {
        EstadoCita.PENDIENTE -> {
            container = MaterialTheme.colorScheme.tertiaryContainer
            content = MaterialTheme.colorScheme.onTertiaryContainer
        }

        EstadoCita.CONFIRMADA -> {
            container = MaterialTheme.colorScheme.primaryContainer
            content = MaterialTheme.colorScheme.onPrimaryContainer
        }

        EstadoCita.CANCELADA -> {
            container = MaterialTheme.colorScheme.errorContainer
            content = MaterialTheme.colorScheme.onErrorContainer
        }
    }
    AssistChip(
        onClick = {},
        label = { Text(stringResource(estado.etiqueta)) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = container,
            labelColor = content,
        ),
        modifier = modifier,
    )
}

/** Par etiqueta / valor para la pantalla de detalle. */
@Composable
fun FilaDetalle(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

/** Indicador del estado de conexión y sincronización. */
@Composable
fun EstadoConexion(conectado: Boolean, modifier: Modifier = Modifier) {
    val container = if (conectado) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val content = if (conectado) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(
        color = container,
        contentColor = content,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(painterResource(R.drawable.ic_sync), contentDescription = null)
            Column {
                Text(
                    text = stringResource(
                        if (conectado) R.string.inicio_conectado else R.string.inicio_sin_conexion
                    ),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.inicio_sync_pendiente),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
