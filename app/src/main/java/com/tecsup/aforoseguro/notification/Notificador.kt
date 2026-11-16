package com.tecsup.aforoseguro.notification

interface Notificador {
    fun avisarAforoLleno(personas: Int, maximo: Int)
}
