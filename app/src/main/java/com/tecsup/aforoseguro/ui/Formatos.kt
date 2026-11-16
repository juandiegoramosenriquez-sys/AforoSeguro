package com.tecsup.aforoseguro.ui

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val HORA_MINUTO = DateTimeFormatter.ofPattern("HH:mm")
private val HORA_COMPLETA = DateTimeFormatter.ofPattern("HH:mm:ss")
private val SEGUNDOS = DateTimeFormatter.ofPattern(":ss")

private fun Long.aHoraLocal() = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault())

fun Long.horaCorta(): String = aHoraLocal().format(HORA_MINUTO)

fun Long.horaCompleta(): String = aHoraLocal().format(HORA_COMPLETA)

fun etiquetasDeTiempo(horas: List<Long>): List<String> = horas.mapIndexed { i, hora ->
    val minuto = hora.horaCorta()
    if (i > 0 && horas[i - 1].horaCorta() == minuto) hora.aHoraLocal().format(SEGUNDOS) else minuto
}

fun Int.contar(singular: String, plural: String): String = "$this ${if (this == 1) singular else plural}"

fun saludoDelDia(hora: LocalTime = LocalTime.now()): String = when (hora.hour) {
    in 5..11 -> "Buenos días"
    in 12..18 -> "Buenas tardes"
    else -> "Buenas noches"
}
