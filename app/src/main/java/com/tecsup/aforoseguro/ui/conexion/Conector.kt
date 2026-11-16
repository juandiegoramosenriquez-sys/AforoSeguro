package com.tecsup.aforoseguro.ui.conexion

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AddLink
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.tecsup.aforoseguro.domain.model.Dispositivo
import com.tecsup.aforoseguro.domain.model.EstadoConexion
import com.tecsup.aforoseguro.ui.components.Insignia
import com.tecsup.aforoseguro.ui.theme.Aviso
import com.tecsup.aforoseguro.ui.theme.AvisoSuave
import com.tecsup.aforoseguro.ui.theme.Exito
import com.tecsup.aforoseguro.ui.theme.ExitoSuave
import com.tecsup.aforoseguro.ui.theme.Fondo
import com.tecsup.aforoseguro.ui.theme.Indigo
import com.tecsup.aforoseguro.ui.theme.IndigoSuave
import com.tecsup.aforoseguro.ui.theme.Peligro
import com.tecsup.aforoseguro.ui.theme.TintaSuave

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Conector(
    viewModel: ConexionViewModel,
    contenido: @Composable (iniciar: () -> Unit) -> Unit
) {
    val contexto = LocalContext.current
    var dispositivos by remember { mutableStateOf<List<Dispositivo>?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }

    val activarBluetooth = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        dispositivos = if (viewModel.bluetoothActivado()) viewModel.dispositivos() else emptyList()
    }

    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        when {
            !concedido -> aviso = "Sin el permiso de dispositivos cercanos la app no puede conectarse al ESP32. " +
                "Igual puedes probar el modo demostración."
            !viewModel.bluetoothActivado() -> activarBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            else -> dispositivos = viewModel.dispositivos()
        }
    }

    val iniciar: () -> Unit = {
        val sinPermiso = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.BLUETOOTH_CONNECT) !=
            PackageManager.PERMISSION_GRANTED
        when {
            sinPermiso -> pedirPermiso.launch(Manifest.permission.BLUETOOTH_CONNECT)
            !viewModel.bluetoothActivado() -> runCatching {
                activarBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }.onFailure { dispositivos = emptyList() }
            else -> dispositivos = viewModel.dispositivos()
        }
    }

    contenido(iniciar)

    dispositivos?.let { lista ->
        ModalBottomSheet(
            onDismissRequest = { dispositivos = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            HojaDispositivos(
                dispositivos = lista,
                bluetoothActivado = viewModel.bluetoothActivado(),
                onElegir = {
                    dispositivos = null
                    viewModel.conectar(it)
                },
                onDemo = {
                    dispositivos = null
                    viewModel.iniciarDemo()
                },
                onEmparejar = {
                    dispositivos = null
                    contexto.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                }
            )
        }
    }

    aviso?.let { texto ->
        AlertDialog(
            onDismissRequest = { aviso = null },
            title = { Text("Permiso necesario") },
            text = { Text(texto) },
            confirmButton = {
                TextButton(onClick = {
                    aviso = null
                    viewModel.iniciarDemo()
                }) { Text("Usar demostración") }
            },
            dismissButton = { TextButton(onClick = { aviso = null }) { Text("Cerrar") } }
        )
    }
}

@Composable
private fun HojaDispositivos(
    dispositivos: List<Dispositivo>,
    bluetoothActivado: Boolean,
    onElegir: (Dispositivo) -> Unit,
    onDemo: () -> Unit,
    onEmparejar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Conectar dispositivo", style = MaterialTheme.typography.titleLarge)
        Text(
            if (bluetoothActivado) "Elige el ESP32 \"AforoSeguro\" de tus dispositivos emparejados."
            else "El Bluetooth está apagado. Actívalo para ver tus dispositivos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Fondo)
        ) {
            if (dispositivos.isEmpty()) {
                Text(
                    "No hay dispositivos emparejados todavía.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            dispositivos.forEachIndexed { indice, dispositivo ->
                val recomendado = dispositivo.nombre.contains("aforo", ignoreCase = true)
                OpcionHoja(
                    icono = Icons.Outlined.Memory,
                    titulo = dispositivo.nombre,
                    detalle = if (recomendado) "Recomendado · ${dispositivo.direccion}" else dispositivo.direccion,
                    color = if (recomendado) Exito else Indigo,
                    fondo = if (recomendado) ExitoSuave else Color.White,
                    onClick = { onElegir(dispositivo) }
                )
                if (indice < dispositivos.lastIndex) HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
            }
        }

        OpcionHoja(
            icono = Icons.Outlined.AddLink,
            titulo = "Emparejar un dispositivo nuevo",
            detalle = "Abre los ajustes de Bluetooth del celular",
            color = Indigo,
            fondo = IndigoSuave,
            onClick = onEmparejar
        )
        OpcionHoja(
            icono = Icons.Outlined.PlayCircle,
            titulo = "Probar en modo demostración",
            detalle = "Simula el ESP32 sin necesidad del prototipo",
            color = Aviso,
            fondo = AvisoSuave,
            onClick = onDemo
        )
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun OpcionHoja(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    color: Color,
    fondo: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Insignia(icono, color = color, fondo = fondo)
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall)
            Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = TintaSuave)
    }
}

@Composable
fun ChipConexion(conexion: EstadoConexion, onClick: () -> Unit) {
    val (texto, color) = when (conexion) {
        is EstadoConexion.Conectado -> (if (conexion.demo) "Demo" else "En línea") to Exito
        is EstadoConexion.Conectando -> "Conectando" to Indigo
        is EstadoConexion.Error -> "Sin señal" to Peligro
        EstadoConexion.Desconectado -> "Desconectado" to TintaSuave
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(texto, style = MaterialTheme.typography.labelLarge, color = color)
    }
}
