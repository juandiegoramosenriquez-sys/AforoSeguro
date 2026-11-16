package com.tecsup.aforoseguro

import com.tecsup.aforoseguro.domain.model.EstadoAforo
import com.tecsup.aforoseguro.domain.model.NivelAforo
import org.junit.Assert.assertEquals
import org.junit.Test

class NivelAforoTest {

    @Test
    fun pocasPersonasEsSeguro() {
        assertEquals(NivelAforo.SEGURO, NivelAforo.de(5, 20))
    }

    @Test
    fun desdeSetentaPorCientoEsAlerta() {
        assertEquals(NivelAforo.ALERTA, NivelAforo.de(14, 20))
        assertEquals(NivelAforo.SEGURO, NivelAforo.de(13, 20))
    }

    @Test
    fun alLlegarAlMaximoEsLleno() {
        assertEquals(NivelAforo.LLENO, NivelAforo.de(20, 20))
        assertEquals(NivelAforo.LLENO, NivelAforo.de(23, 20))
    }

    @Test
    fun sinMaximoConfiguradoNoFalla() {
        assertEquals(NivelAforo.SEGURO, NivelAforo.de(3, 0))
        assertEquals(0, EstadoAforo(personas = 3, maximo = 0).porcentaje)
    }

    @Test
    fun porcentajeSeCalculaSobreElMaximo() {
        assertEquals(50, EstadoAforo(personas = 10, maximo = 20).porcentaje)
    }
}
