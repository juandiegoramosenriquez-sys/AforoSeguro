package com.tecsup.aforoseguro.data

import com.tecsup.aforoseguro.domain.model.Dispositivo
import com.tecsup.aforoseguro.domain.model.EstadoAforo
import com.tecsup.aforoseguro.domain.model.EstadoConexion
import com.tecsup.aforoseguro.domain.model.Evento
import kotlinx.coroutines.flow.StateFlow

interface AforoRepository {
    val conexion: StateFlow<EstadoConexion>
    val estado: StateFlow<EstadoAforo?>
    val historial: StateFlow<List<Evento>>
    fun bluetoothActivado(): Boolean
    fun dispositivosEmparejados(): List<Dispositivo>
    fun conectar(dispositivo: Dispositivo)
    fun iniciarDemo()
    fun desconectar()
    suspend fun cambiarMaximo(maximo: Int): Result<Unit>
    suspend fun reiniciarConteo(): Result<Unit>
}
