package com.tecsup.aforoseguro.domain.model

data class EstadoAforo(
    val personas: Int = 0,
    val maximo: Int = 0,
    val entradas: Int = 0,
    val salidas: Int = 0
) {
    val porcentaje: Int get() = if (maximo <= 0) 0 else personas * 100 / maximo
    val nivel: NivelAforo get() = NivelAforo.de(personas, maximo)
}

enum class NivelAforo {
    SEGURO, ALERTA, LLENO;

    companion object {
        const val PORCENTAJE_ALERTA = 70

        fun de(personas: Int, maximo: Int): NivelAforo = when {
            maximo <= 0 -> SEGURO
            personas >= maximo -> LLENO
            personas * 100 >= maximo * PORCENTAJE_ALERTA -> ALERTA
            else -> SEGURO
        }
    }
}
