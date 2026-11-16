package com.tecsup.aforoseguro.ui.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.aforoseguro.domain.model.Evento
import com.tecsup.aforoseguro.domain.model.TipoEvento
import com.tecsup.aforoseguro.ui.components.ContadorAnimado
import com.tecsup.aforoseguro.ui.components.Encabezado
import com.tecsup.aforoseguro.ui.components.EstadoVacio
import com.tecsup.aforoseguro.ui.components.Insignia
import com.tecsup.aforoseguro.ui.components.estiloTarjeta
import com.tecsup.aforoseguro.ui.contar
import com.tecsup.aforoseguro.ui.horaCompleta
import com.tecsup.aforoseguro.ui.theme.Exito
import com.tecsup.aforoseguro.ui.theme.ExitoSuave
import com.tecsup.aforoseguro.ui.theme.Indigo
import com.tecsup.aforoseguro.ui.theme.IndigoSuave
import com.tecsup.aforoseguro.ui.theme.Peligro
import com.tecsup.aforoseguro.ui.theme.PeligroSuave

@Composable
fun HistorialScreen(viewModel: HistorialViewModel) {
    val historial by viewModel.historial.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Encabezado(titulo = "Historial", subtitulo = "Movimientos registrados en esta sesión")
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Resumen(Icons.Outlined.SwapVert, "Movimientos", historial.total, Indigo, IndigoSuave, Modifier.weight(1f))
                Resumen(Icons.AutoMirrored.Outlined.Login, "Entradas", historial.entradas, Exito, ExitoSuave, Modifier.weight(1f))
                Resumen(Icons.AutoMirrored.Outlined.Logout, "Salidas", historial.salidas, Peligro, PeligroSuave, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Filtro.entries.forEach { filtro ->
                    FilterChip(
                        selected = historial.filtro == filtro,
                        onClick = { viewModel.filtrar(filtro) },
                        label = { Text(filtro.etiqueta) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Indigo,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
        if (historial.eventos.isEmpty()) {
            item {
                EstadoVacio(
                    icono = Icons.Outlined.History,
                    titulo = if (historial.total == 0) "Aún no hay movimientos" else "Sin resultados",
                    detalle = if (historial.total == 0) {
                        "Con la app conectada, cada entrada y salida aparecerá aquí al instante."
                    } else {
                        "No hay ${historial.filtro.etiqueta.lowercase()} registradas."
                    }
                )
            }
        } else {
            items(historial.eventos, key = { it.id }) { evento ->
                FilaEvento(evento, Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun Resumen(
    icono: ImageVector,
    titulo: String,
    valor: Int,
    color: Color,
    fondo: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .estiloTarjeta()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Insignia(icono, color = color, fondo = fondo, tamano = 34.dp)
        ContadorAnimado(valor, MaterialTheme.typography.titleLarge)
        Text(titulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FilaEvento(evento: Evento, modifier: Modifier = Modifier) {
    val entrada = evento.tipo == TipoEvento.ENTRADA
    Row(
        modifier = modifier
            .fillMaxWidth()
            .estiloTarjeta()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Insignia(
            icono = if (entrada) Icons.AutoMirrored.Outlined.Login else Icons.AutoMirrored.Outlined.Logout,
            color = if (entrada) Exito else Peligro,
            fondo = if (entrada) ExitoSuave else PeligroSuave
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(if (entrada) "Entrada registrada" else "Salida registrada", style = MaterialTheme.typography.titleSmall)
            Text(
                "Aforo: ${evento.personas.contar("persona", "personas")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            evento.hora.horaCompleta(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
