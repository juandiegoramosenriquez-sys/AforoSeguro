package com.tecsup.aforoseguro

import com.tecsup.aforoseguro.data.SimuladorEsp32
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SimuladorEsp32Test {

    private fun simuladorQuieto() = SimuladorEsp32(Random(1), pausaMinMs = 60_000, pausaMaxMs = 60_001)

    @Test
    fun empiezaVacioConAforoDeVeinte() = runBlocking {
        assertEquals("ESTADO;0;20;0;0", simuladorQuieto().lineas().first())
    }

    @Test
    fun respondeAlCambioDeMaximo() = runBlocking {
        val simulador = simuladorQuieto()
        simulador.recibir("MAX;5")
        assertEquals(listOf("ESTADO;0;20;0;0", "ESTADO;0;5;0;0"), simulador.lineas().take(2).toList())
    }

    @Test
    fun ignoraMaximosInvalidos() = runBlocking {
        val simulador = simuladorQuieto()
        simulador.recibir("MAX;0")
        assertEquals("ESTADO;0;20;0;0", simulador.lineas().take(2).toList().last())
    }

    @Test
    fun generaMovimientosValidos() = runBlocking {
        val simulador = SimuladorEsp32(Random(7), pausaMinMs = 1, pausaMaxMs = 2)
        val lineas = simulador.lineas().take(41).toList()
        val personas = lineas.filter { it.startsWith("ESTADO") }.map { it.split(";")[1].toInt() }
        assertTrue(personas.all { it >= 0 })
        assertTrue(lineas.drop(1).filterIndexed { i, _ -> i % 2 == 0 }.all { it.startsWith("EVENTO;") })
    }
}
