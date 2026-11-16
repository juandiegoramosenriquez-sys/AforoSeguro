package com.tecsup.aforoseguro.data

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class SimuladorEsp32(
    private val aleatorio: Random = Random.Default,
    private val pausaMinMs: Long = 1_800L,
    private val pausaMaxMs: Long = 3_800L
) {
    private val ordenes = Channel<String>(Channel.UNLIMITED)

    private var personas = 0
    private var maximo = 20
    private var entradas = 0
    private var salidas = 0

    fun recibir(orden: String) {
        ordenes.trySend(orden.trim())
    }

    fun lineas(): Flow<String> = channelFlow {
        send(estado())
        launch {
            for (orden in ordenes) {
                aplicar(orden)
                send(estado())
            }
        }
        while (true) {
            delay(aleatorio.nextLong(pausaMinMs, pausaMaxMs))
            send(simularPaso())
            send(estado())
        }
    }

    @Synchronized
    private fun aplicar(orden: String) {
        when {
            orden.startsWith("MAX;") -> orden.substringAfter(";").toIntOrNull()
                ?.takeIf { it in 1..999 }
                ?.let { maximo = it }
            orden == Protocolo.REINICIAR -> {
                personas = 0
                entradas = 0
                salidas = 0
            }
        }
    }

    @Synchronized
    private fun simularPaso(): String {
        val ocupacion = personas.toFloat() / maximo
        val probabilidadEntrada = when {
            personas == 0 -> 1f
            ocupacion < 0.5f -> 0.75f
            ocupacion < 0.9f -> 0.55f
            else -> 0.3f
        }
        return if (aleatorio.nextFloat() < probabilidadEntrada) {
            personas++
            entradas++
            "EVENTO;ENTRADA;$personas"
        } else {
            personas--
            salidas++
            "EVENTO;SALIDA;$personas"
        }
    }

    @Synchronized
    private fun estado(): String = "ESTADO;$personas;$maximo;$entradas;$salidas"
}
