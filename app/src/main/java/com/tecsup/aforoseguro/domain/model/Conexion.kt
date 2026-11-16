package com.tecsup.aforoseguro.domain.model

data class Dispositivo(val nombre: String, val direccion: String)

sealed interface EstadoConexion {
    data object Desconectado : EstadoConexion
    data class Conectando(val nombre: String) : EstadoConexion
    data class Conectado(val nombre: String, val demo: Boolean = false) : EstadoConexion
    data class Error(val mensaje: String) : EstadoConexion
}
