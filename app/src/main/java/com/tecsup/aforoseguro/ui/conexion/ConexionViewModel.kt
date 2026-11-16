package com.tecsup.aforoseguro.ui.conexion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.aforoseguro.AppContainer
import com.tecsup.aforoseguro.data.AforoRepository
import com.tecsup.aforoseguro.domain.model.Dispositivo
import com.tecsup.aforoseguro.domain.model.EstadoConexion
import kotlinx.coroutines.flow.StateFlow

class ConexionViewModel(private val repo: AforoRepository) : ViewModel() {

    val conexion: StateFlow<EstadoConexion> = repo.conexion

    fun bluetoothActivado(): Boolean = repo.bluetoothActivado()

    fun dispositivos(): List<Dispositivo> = repo.dispositivosEmparejados()

    fun conectar(dispositivo: Dispositivo) = repo.conectar(dispositivo)

    fun iniciarDemo() = repo.iniciarDemo()

    fun desconectar() = repo.desconectar()

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { ConexionViewModel(container.aforoRepository) }
        }
    }
}
