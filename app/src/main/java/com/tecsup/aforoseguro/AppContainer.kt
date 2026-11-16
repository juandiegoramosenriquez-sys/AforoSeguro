package com.tecsup.aforoseguro

import android.content.Context
import com.tecsup.aforoseguro.data.AforoRepository
import com.tecsup.aforoseguro.data.BluetoothAforoRepository
import com.tecsup.aforoseguro.notification.Notificador
import com.tecsup.aforoseguro.notification.NotificadorAndroid

class AppContainer(context: Context) {

    private val contextoApp = context.applicationContext

    val aforoRepository: AforoRepository by lazy {
        BluetoothAforoRepository(contextoApp)
    }

    val notificador: Notificador by lazy {
        NotificadorAndroid(contextoApp)
    }
}
