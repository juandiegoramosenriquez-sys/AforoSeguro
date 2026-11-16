package com.tecsup.aforoseguro

import com.tecsup.aforoseguro.data.Mensaje
import com.tecsup.aforoseguro.data.Protocolo
import com.tecsup.aforoseguro.domain.model.EstadoAforo
import com.tecsup.aforoseguro.domain.model.TipoEvento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProtocoloTest {

    @Test
    fun leeElEstadoQueEnviaElEsp32() {
        assertEquals(
            Mensaje.Estado(EstadoAforo(personas = 8, maximo = 20, entradas = 12, salidas = 4)),
            Protocolo.leer("ESTADO;8;20;12;4\r")
        )
    }

    @Test
    fun leeEntradasYSalidas() {
        assertEquals(Mensaje.Movimiento(TipoEvento.ENTRADA, 9), Protocolo.leer("EVENTO;ENTRADA;9"))
        assertEquals(Mensaje.Movimiento(TipoEvento.SALIDA, 7), Protocolo.leer("EVENTO;SALIDA;7"))
    }

    @Test
    fun ignoraMensajesIncompletosODanados() {
        assertNull(Protocolo.leer(""))
        assertNull(Protocolo.leer("ESTADO;8;20"))
        assertNull(Protocolo.leer("ESTADO;8;x;12;4"))
        assertNull(Protocolo.leer("EVENTO;SALTO;3"))
        assertNull(Protocolo.leer("HOLA"))
    }

    @Test
    fun armaLasOrdenesParaElEsp32() {
        assertEquals("MAX;15", Protocolo.cambiarMaximo(15))
        assertEquals("RESET", Protocolo.REINICIAR)
    }
}
