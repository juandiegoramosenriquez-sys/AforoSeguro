package com.tecsup.aforoseguro.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.tecsup.aforoseguro.domain.model.Dispositivo
import com.tecsup.aforoseguro.domain.model.EstadoAforo
import com.tecsup.aforoseguro.domain.model.EstadoConexion
import com.tecsup.aforoseguro.domain.model.Evento
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothAforoRepository(context: Context) : AforoRepository {

    private val adaptador: BluetoothAdapter? =
        context.getSystemService(BluetoothManager::class.java)?.adapter

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val escritura = Mutex()
    private var trabajo: Job? = null
    private var socket: BluetoothSocket? = null
    private var simulador: SimuladorEsp32? = null
    private var siguienteId = 0L

    private val _conexion = MutableStateFlow<EstadoConexion>(EstadoConexion.Desconectado)
    override val conexion: StateFlow<EstadoConexion> = _conexion.asStateFlow()

    private val _estado = MutableStateFlow<EstadoAforo?>(null)
    override val estado: StateFlow<EstadoAforo?> = _estado.asStateFlow()

    private val _historial = MutableStateFlow<List<Evento>>(emptyList())
    override val historial: StateFlow<List<Evento>> = _historial.asStateFlow()

    override fun bluetoothActivado(): Boolean = adaptador?.isEnabled == true

    override fun dispositivosEmparejados(): List<Dispositivo> = try {
        adaptador?.bondedDevices.orEmpty()
            .map { Dispositivo(it.name ?: it.address, it.address) }
            .sortedByDescending { it.nombre.contains("aforo", ignoreCase = true) }
    } catch (e: SecurityException) {
        emptyList()
    }

    override fun conectar(dispositivo: Dispositivo) {
        desconectar()
        val bluetooth = adaptador ?: run {
            _conexion.value = EstadoConexion.Error("Este celular no tiene Bluetooth")
            return
        }
        trabajo = alcance.launch {
            _conexion.value = EstadoConexion.Conectando(dispositivo.nombre)
            var nuevo: BluetoothSocket? = null
            try {
                nuevo = bluetooth.getRemoteDevice(dispositivo.direccion)
                    .createRfcommSocketToServiceRecord(UUID_SPP)
                socket = nuevo
                nuevo.connect()
                _conexion.value = EstadoConexion.Conectado(dispositivo.nombre)
                nuevo.inputStream.bufferedReader().use { lector ->
                    while (isActive) {
                        val linea = lector.readLine() ?: break
                        procesar(linea)
                    }
                }
                if (isActive) _conexion.value = EstadoConexion.Error("Se perdió la conexión con ${dispositivo.nombre}")
            } catch (e: Exception) {
                if (isActive) {
                    _conexion.value = EstadoConexion.Error(
                        "No se pudo conectar con ${dispositivo.nombre}. Revisa que el ESP32 esté encendido y cerca."
                    )
                }
            } finally {
                runCatching { nuevo?.close() }
                if (socket === nuevo) socket = null
            }
        }
    }

    override fun iniciarDemo() {
        desconectar()
        reiniciarDatos()
        val demo = SimuladorEsp32()
        simulador = demo
        _conexion.value = EstadoConexion.Conectado(NOMBRE_DEMO, demo = true)
        trabajo = alcance.launch {
            demo.lineas().collect { procesar(it) }
        }
    }

    override fun desconectar() {
        val eraDemo = simulador != null
        trabajo?.cancel()
        trabajo = null
        simulador = null
        cerrarSocket()
        _conexion.value = EstadoConexion.Desconectado
        if (eraDemo) reiniciarDatos()
    }

    override suspend fun cambiarMaximo(maximo: Int): Result<Unit> = enviar(Protocolo.cambiarMaximo(maximo))

    override suspend fun reiniciarConteo(): Result<Unit> {
        val resultado = enviar(Protocolo.REINICIAR)
        if (resultado.isSuccess) _historial.value = emptyList()
        return resultado
    }

    private fun procesar(linea: String) {
        when (val mensaje = Protocolo.leer(linea)) {
            is Mensaje.Estado -> _estado.value = mensaje.estado
            is Mensaje.Movimiento -> _historial.update { anteriores ->
                val evento = Evento(siguienteId++, mensaje.tipo, mensaje.personas, System.currentTimeMillis())
                (listOf(evento) + anteriores).take(LIMITE_HISTORIAL)
            }
            null -> Unit
        }
    }

    private suspend fun enviar(texto: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            check(_conexion.value is EstadoConexion.Conectado) { "Primero conéctate al ESP32" }
            simulador?.let {
                it.recibir(texto)
                return@runCatching
            }
            escritura.withLock {
                val actual = checkNotNull(socket) { "Primero conéctate al ESP32" }
                actual.outputStream.write("$texto\n".toByteArray())
                actual.outputStream.flush()
            }
        }
    }

    private fun reiniciarDatos() {
        _estado.value = null
        _historial.value = emptyList()
    }

    private fun cerrarSocket() {
        runCatching { socket?.close() }
        socket = null
    }

    companion object {
        private val UUID_SPP: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val LIMITE_HISTORIAL = 100
        const val NOMBRE_DEMO = "Modo demostración"
    }
}
