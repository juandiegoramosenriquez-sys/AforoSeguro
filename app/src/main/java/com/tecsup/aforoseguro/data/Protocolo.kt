package com.tecsup.aforoseguro.data

import com.tecsup.aforoseguro.domain.model.EstadoAforo
import com.tecsup.aforoseguro.domain.model.TipoEvento

sealed interface Mensaje {
    data class Estado(val estado: EstadoAforo) : Mensaje
    data class Movimiento(val tipo: TipoEvento, val personas: Int) : Mensaje
}

object Protocolo {

    const val REINICIAR = "RESET"

    fun cambiarMaximo(maximo: Int) = "MAX;$maximo"

    fun leer(linea: String): Mensaje? {
        val partes = linea.trim().split(";")
        return when (partes.firstOrNull()) {
            "ESTADO" -> leerEstado(partes)
            "EVENTO" -> leerEvento(partes)
            else -> null
        }
    }

    private fun leerEstado(partes: List<String>): Mensaje? {
        if (partes.size != 5) return null
        val numeros = partes.drop(1).map { it.toIntOrNull() ?: return null }
        return Mensaje.Estado(EstadoAforo(numeros[0], numeros[1], numeros[2], numeros[3]))
    }

    private fun leerEvento(partes: List<String>): Mensaje? {
        if (partes.size != 3) return null
        val tipo = when (partes[1]) {
            "ENTRADA" -> TipoEvento.ENTRADA
            "SALIDA" -> TipoEvento.SALIDA
            else -> return null
        }
        val personas = partes[2].toIntOrNull() ?: return null
        return Mensaje.Movimiento(tipo, personas)
    }
}
