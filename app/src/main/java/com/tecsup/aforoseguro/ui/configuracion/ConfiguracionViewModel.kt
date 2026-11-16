package com.tecsup.aforoseguro.ui.configuracion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.aforoseguro.AppContainer
import com.tecsup.aforoseguro.data.AforoRepository
import com.tecsup.aforoseguro.domain.model.EstadoConexion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class Ajustes(
    val conexion: EstadoConexion = EstadoConexion.Desconectado,
    val maximoActual: Int? = null
) {
    val conectado: Boolean get() = conexion is EstadoConexion.Conectado
}

class ConfiguracionViewModel(private val repo: AforoRepository) : ViewModel() {

    val ajustes: StateFlow<Ajustes> = combine(repo.conexion, repo.estado) { conexion, estado ->
        Ajustes(conexion, estado?.maximo)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Ajustes())

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _enviando = MutableStateFlow(false)
    val enviando: StateFlow<Boolean> = _enviando.asStateFlow()

    fun guardarMaximo(maximo: Int) {
        if (maximo !in MINIMO..MAXIMO) {
            _mensaje.value = "El aforo debe estar entre $MINIMO y $MAXIMO personas"
            return
        }
        ejecutar("Aforo máximo actualizado a $maximo personas") { repo.cambiarMaximo(maximo) }
    }

    fun reiniciarConteo() {
        ejecutar("Conteo reiniciado") { repo.reiniciarConteo() }
    }

    fun mensajeMostrado() {
        _mensaje.value = null
    }

    private fun ejecutar(exito: String, accion: suspend () -> Result<Unit>) {
        if (_enviando.value) return
        viewModelScope.launch {
            _enviando.value = true
            _mensaje.value = accion().fold(
                onSuccess = { exito },
                onFailure = { it.message ?: "No se pudo enviar la orden" }
            )
            _enviando.value = false
        }
    }

    companion object {
        const val MINIMO = 1
        const val MAXIMO = 999

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { ConfiguracionViewModel(container.aforoRepository) }
        }
    }
}
