package com.tecsup.aforoseguro.ui.monitor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.SensorsOff
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.aforoseguro.domain.model.EstadoAforo
import com.tecsup.aforoseguro.domain.model.EstadoConexion
import com.tecsup.aforoseguro.domain.model.NivelAforo
import com.tecsup.aforoseguro.ui.components.Banner
import com.tecsup.aforoseguro.ui.components.ContadorAnimado
import com.tecsup.aforoseguro.ui.components.Encabezado
import com.tecsup.aforoseguro.ui.components.GraficoBarras
import com.tecsup.aforoseguro.ui.components.Insignia
import com.tecsup.aforoseguro.ui.components.Pildora
import com.tecsup.aforoseguro.ui.components.Tarjeta
import com.tecsup.aforoseguro.ui.components.estiloTarjeta
import com.tecsup.aforoseguro.ui.conexion.ChipConexion
import com.tecsup.aforoseguro.ui.contar
import com.tecsup.aforoseguro.ui.saludoDelDia
import com.tecsup.aforoseguro.ui.theme.Aviso
import com.tecsup.aforoseguro.ui.theme.AvisoSuave
import com.tecsup.aforoseguro.ui.theme.Exito
import com.tecsup.aforoseguro.ui.theme.ExitoSuave
import com.tecsup.aforoseguro.ui.theme.Indigo
import com.tecsup.aforoseguro.ui.theme.IndigoClaro
import com.tecsup.aforoseguro.ui.theme.IndigoProfundo
import com.tecsup.aforoseguro.ui.theme.IndigoSuave
import com.tecsup.aforoseguro.ui.theme.Peligro
import com.tecsup.aforoseguro.ui.theme.PeligroSuave
import com.tecsup.aforoseguro.ui.theme.paleta

@Composable
fun MonitorScreen(
    viewModel: MonitorViewModel,
    onConectar: () -> Unit,
    onDemo: () -> Unit
) {
    val panel by viewModel.panel.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Encabezado(
            antetitulo = saludoDelDia(),
            titulo = "Panel de aforo",
            subtitulo = "Resumen del aforo en tiempo real"
        ) {
            ChipConexion(panel.conexion, onClick = onConectar)
        }

        val estado = panel.estado
        if (estado == null) {
            Bienvenida(panel.conexion, onConectar, onDemo)
            PasosConexion()
        } else {
            AnimatedVisibility(
                visible = !panel.conectado,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Banner(
                    icono = Icons.Outlined.SensorsOff,
                    titulo = "Sin conexión con el ESP32",
                    detalle = "Se muestra el último dato recibido.",
                    color = Peligro,
                    fondo = PeligroSuave
                ) {
                    TextButton(onClick = onConectar) { Text("Conectar") }
                }
            }
            TarjetaPrincipal(estado, demo = (panel.conexion as? EstadoConexion.Conectado)?.demo == true)
            Indicadores(estado)
            AvisoNivel(estado)
            TarjetaGrafico(panel)
        }
    }
}

@Composable
private fun Bienvenida(conexion: EstadoConexion, onConectar: () -> Unit, onDemo: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(IndigoProfundo, Indigo, IndigoClaro)))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Insignia(Icons.Outlined.Bluetooth, color = Color.White, fondo = Color.White.copy(alpha = 0.18f), tamano = 56.dp)
        Text(
            when (conexion) {
                is EstadoConexion.Conectando -> "Conectando con ${conexion.nombre}…"
                is EstadoConexion.Conectado -> "Esperando datos del ESP32…"
                else -> "Conecta tu Aforo Seguro"
            },
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )
        Text(
            (conexion as? EstadoConexion.Error)?.mensaje
                ?: "Enlaza el celular con el ESP32 por Bluetooth para ver cuántas personas hay en el local y controlar el aforo.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f)
        )
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = onConectar,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Indigo),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Outlined.Bluetooth, null)
            Spacer(Modifier.size(8.dp))
            Text("Conectar por Bluetooth", style = MaterialTheme.typography.titleSmall)
        }
        OutlinedButton(
            onClick = onDemo,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Outlined.PlayCircle, null)
            Spacer(Modifier.size(8.dp))
            Text("Probar modo demostración", style = MaterialTheme.typography.titleSmall)
        }
    }
}

private val PASOS = listOf(
    "Enciende el ESP32" to "El LCD mostrará \"Aforo Seguro\".",
    "Empareja el celular" to "En Ajustes del celular → Bluetooth, toca \"AforoSeguro\".",
    "Conéctate desde la app" to "Pulsa Conectar y elige el dispositivo."
)

@Composable
private fun PasosConexion() {
    Tarjeta(espacio = 14.dp) {
        Text("¿Cómo conectarse?", style = MaterialTheme.typography.titleMedium)
        PASOS.forEachIndexed { indice, (titulo, detalle) ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(IndigoSuave, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${indice + 1}", style = MaterialTheme.typography.titleSmall, color = Indigo)
                }
                Column {
                    Text(titulo, style = MaterialTheme.typography.titleSmall)
                    Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun TarjetaPrincipal(estado: EstadoAforo, demo: Boolean) {
    val paleta = estado.nivel.paleta()
    val inicio by animateColorAsState(paleta.inicio, label = "inicio")
    val fin by animateColorAsState(paleta.fin, label = "fin")
    val progreso by animateFloatAsState(
        targetValue = if (estado.maximo <= 0) 0f else (estado.personas.toFloat() / estado.maximo).coerceIn(0f, 1f),
        label = "progreso"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(inicio, fin)))
    ) {
        Box(
            Modifier
                .size(180.dp)
                .offset(x = 200.dp, y = (-60).dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )
        Box(
            Modifier
                .size(120.dp)
                .offset(x = 250.dp, y = 110.dp)
                .background(Color.White.copy(alpha = 0.06f), CircleShape)
        )
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Personas dentro",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.weight(1f)
                )
                Pildora(
                    texto = if (demo) "${paleta.etiqueta} · demo" else paleta.etiqueta,
                    color = Color.White,
                    fondo = Color.White.copy(alpha = 0.2f)
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                ContadorAnimado(estado.personas, MaterialTheme.typography.displayLarge, Color.White)
                Text(
                    "  / ${estado.maximo}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.25f))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progreso)
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                )
            }
            Row {
                Text(
                    "${estado.porcentaje}% de ocupación",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${(estado.maximo - estado.personas).coerceAtLeast(0).contar("espacio libre", "espacios libres")}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun Indicadores(estado: EstadoAforo) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Indicador(Icons.Outlined.Groups, "Capacidad", estado.maximo, Indigo, IndigoSuave, Modifier.weight(1f))
        Indicador(Icons.AutoMirrored.Outlined.Login, "Entradas", estado.entradas, Exito, ExitoSuave, Modifier.weight(1f))
        Indicador(Icons.AutoMirrored.Outlined.Logout, "Salidas", estado.salidas, Peligro, PeligroSuave, Modifier.weight(1f))
    }
}

@Composable
private fun Indicador(
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Insignia(icono, color = color, fondo = fondo, tamano = 38.dp)
        ContadorAnimado(valor, MaterialTheme.typography.headlineSmall)
        Text(titulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AvisoNivel(estado: EstadoAforo) {
    val libres = (estado.maximo - estado.personas).coerceAtLeast(0)
    when (estado.nivel) {
        NivelAforo.SEGURO -> Banner(
            icono = Icons.Outlined.CheckCircle,
            titulo = "Aforo dentro del rango seguro",
            detalle = "Todo se encuentra en orden.",
            color = Exito,
            fondo = ExitoSuave
        )
        NivelAforo.ALERTA -> Banner(
            icono = Icons.Outlined.Warning,
            titulo = "Aforo cerca del límite",
            detalle = "Quedan ${libres.contar("espacio", "espacios")}. Controla los ingresos.",
            color = Aviso,
            fondo = AvisoSuave
        )
        NivelAforo.LLENO -> Banner(
            icono = Icons.Outlined.Report,
            titulo = "Aforo completo",
            detalle = "No se debe permitir más ingresos.",
            color = Peligro,
            fondo = PeligroSuave
        )
    }
}

@Composable
private fun TarjetaGrafico(panel: Panel) {
    Tarjeta(espacio = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Evolución del aforo", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Personas dentro tras cada movimiento",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Pildora(
                texto = if (panel.conectado) "En vivo" else "Pausado",
                color = if (panel.conectado) Exito else MaterialTheme.colorScheme.onSurfaceVariant,
                fondo = if (panel.conectado) ExitoSuave else MaterialTheme.colorScheme.background
            )
        }
        if (panel.barras.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.BarChart, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.size(8.dp))
                Text(
                    "El gráfico aparecerá con las primeras entradas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            GraficoBarras(panel.barras, limite = panel.estado?.maximo ?: 0)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(width = 14.dp, height = 3.dp)
                        .background(Peligro.copy(alpha = 0.6f))
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    "Aforo máximo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
