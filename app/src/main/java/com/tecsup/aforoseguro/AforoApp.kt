package com.tecsup.aforoseguro

import android.app.Application

class AforoApp : Application() {

    val container: AppContainer by lazy { AppContainer(this) }
}
