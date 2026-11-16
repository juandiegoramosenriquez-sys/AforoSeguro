package com.tecsup.aforoseguro.domain.model

enum class TipoEvento { ENTRADA, SALIDA }

data class Evento(
    val id: Long,
    val tipo: TipoEvento,
    val personas: Int,
    val hora: Long
)
