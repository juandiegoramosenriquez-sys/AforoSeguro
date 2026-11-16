package com.tecsup.aforoseguro.ui.configuracion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.BluetoothConnected
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.aforoseguro.BuildConfig
import com.tecsup.aforoseguro.domain.model.EstadoConexion
import com.tecsup.aforoseguro.ui.components.ContadorAnimado
import com.tecsup.aforoseguro.ui.components.Encabezado
import com.tecsup.aforoseguro.ui.components.Insignia
import com.tecsup.aforoseguro.ui.components.Tarjeta
import com.tecsup.aforoseguro.ui.theme.Aviso
import com.tecsup.aforoseguro.ui.theme.AvisoSuave
import com.tecsup.aforoseguro.ui.theme.Exito
import com.tecsup.aforoseguro.ui.theme.ExitoSuave
import com.tecsup.aforoseguro.ui.theme.Fondo
import com.tecsup.aforoseguro.ui.theme.Indigo
import com.tecsup.aforoseguro.ui.theme.IndigoSuave
import com.tecsup.aforoseguro.ui.theme.Peligro
import com.tecsup.aforoseguro.ui.theme.PeligroSuave

private val SUGERENCIAS = listOf(10, 20, 30, 50)

@Composable
fun ConfiguracionScreen(
    viewModel: ConfiguracionViewModel,
    onConectar: () -> Unit,
    onDemo: () -> Unit,
    onDesconectar: () -> Unit
) {
    val ajustes by viewModel.ajustes.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val enviando by viewModel.enviando.collectAsStateWithLifecycle()
    val avisos = remember { SnackbarHostState() }
    var confirmarReinicio by remember { mutableStateOf(false) }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            avisos.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Encabezado(titulo = "Ajustes", subtitulo = "Controla el ESP32 desde tu celular")

            TarjetaDispositivo(ajustes.conexion, onConectar, onDemo, onDesconectar)

            TarjetaCapacidad(
                maximoActual = ajustes.maximoActual,
                habilitado = ajustes.conectado && !enviando,
                onGuardar = viewModel::guardarMaximo
            )

            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Insignia(Icons.Outlined.RestartAlt, color = Peligro, fondo = PeligroSuave)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reiniciar conteo", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Pone en 0 las personas, entradas y salidas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                OutlinedButton(
                    onClick = { confirmarReinicio = true },
                    enabled = ajustes.conectado && !enviando,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Peligro)
                ) {
                    Text("Reiniciar ahora")
                }
            }

            AcercaDe()
        }

        SnackbarHost(
            avisos,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    if (confirmarReinicio) {
        AlertDialog(
            onDismissRequest = { confirmarReinicio = false },
            icon = { Icon(Icons.Outlined.RestartAlt, null, tint = Peligro) },
            title = { Text("¿Reiniciar el conteo?") },
            text = { Text("El aforo volverá a 0 en el ESP32 y en la app. Hazlo cuando el local esté vacío.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarReinicio = false
                    viewModel.reiniciarConteo()
                }) { Text("Reiniciar", color = Peligro) }
            },
            dismissButton = { TextButton(onClick = { confirmarReinicio = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun TarjetaDispositivo(
    conexion: EstadoConexion,
    onConectar: () -> Unit,
    onDemo: () -> Unit,
    onDesconectar: () -> Unit
) {
    val conectado = conexion as? EstadoConexion.Conectado
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Insignia(
                icono = if (conectado != null) Icons.Outlined.BluetoothConnected else Icons.Outlined.Bluetooth,
                color = if (conectado != null) Exito else Indigo,
                fondo = if (conectado != null) ExitoSuave else IndigoSuave
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Dispositivo", style = MaterialTheme.typography.titleMedium)
                Text(
                    when (conexion) {
                        is EstadoConexion.Conectado -> "Conectado a ${conexion.nombre}"
                        is EstadoConexion.Conectando -> "Conectando con ${conexion.nombre}…"
                        is EstadoConexion.Error -> conexion.mensaje
                        EstadoConexion.Desconectado -> "Sin conexión"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (conexion is EstadoConexion.Conectado || conexion is EstadoConexion.Conectando) {
            OutlinedButton(
                onClick = onDesconectar,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Desconectar") }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onConectar,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Outlined.Bluetooth, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Conectar")
                }
                OutlinedButton(
                    onClick = onDemo,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Aviso)
                ) {
                    Icon(Icons.Outlined.PlayCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Demo")
                }
            }
        }
    }
}

@Composable
private fun TarjetaCapacidad(
    maximoActual: Int?,
    habilitado: Boolean,
    onGuardar: (Int) -> Unit
) {
    var valor by rememberSaveable { mutableIntStateOf(maximoActual ?: 20) }
    var sincronizado by rememberSaveable { mutableStateOf(maximoActual != null) }

    LaunchedEffect(maximoActual) {
        if (maximoActual != null && !sincronizado) {
            valor = maximoActual
            sincronizado = true
        }
    }

    Tarjeta(espacio = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Insignia(Icons.Outlined.Groups)
            Column(modifier = Modifier.weight(1f)) {
                Text("Capacidad máxima", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Actual en el ESP32: ${maximoActual?.let { "$it personas" } ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Fondo, RoundedCornerShape(18.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = { valor = (valor - 1).coerceAtLeast(ConfiguracionViewModel.MINIMO) },
                enabled = habilitado
            ) { Icon(Icons.Outlined.Remove, "Restar") }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                ContadorAnimado(valor, MaterialTheme.typography.headlineLarge)
                Text("personas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledTonalIconButton(
                onClick = { valor = (valor + 1).coerceAtMost(ConfiguracionViewModel.MAXIMO) },
                enabled = habilitado
            ) { Icon(Icons.Outlined.Add, "Sumar") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SUGERENCIAS.forEach { sugerencia ->
                SuggestionChip(
                    onClick = { valor = sugerencia },
                    enabled = habilitado,
                    label = { Text("$sugerencia") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (valor == sugerencia) IndigoSuave else Color.Transparent,
                        labelColor = if (valor == sugerencia) Indigo else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Button(
            onClick = { onGuardar(valor) },
            enabled = habilitado && valor != maximoActual,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(if (habilitado) "Aplicar al ESP32" else "Conéctate para aplicar cambios")
        }
    }
}

@Composable
private fun AcercaDe() {
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Insignia(Icons.Outlined.Info, color = Aviso, fondo = AvisoSuave)
            Column(modifier = Modifier.weight(1f)) {
                Text("Aforo Seguro", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Versión ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            "Sistema de control de aforo con ESP32, sensores ultrasónicos y conexión Bluetooth.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Equipo 4 · Investigación e Innovación Tecnológica · Tecsup 2026",
            style = MaterialTheme.typography.labelMedium,
            color = Indigo,
            fontWeight = FontWeight.SemiBold
        )
    }
}
