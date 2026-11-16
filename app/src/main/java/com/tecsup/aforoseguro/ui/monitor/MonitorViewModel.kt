package com.tecsup.aforoseguro.ui.monitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.aforoseguro.AppContainer
import com.tecsup.aforoseguro.data.AforoRepository
import com.tecsup.aforoseguro.domain.model.EstadoAforo
import com.tecsup.aforoseguro.domain.model.EstadoConexion
import com.tecsup.aforoseguro.domain.model.Evento
import com.tecsup.aforoseguro.ui.components.Barra
import com.tecsup.aforoseguro.ui.etiquetasDeTiempo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class Panel(
    val conexion: EstadoConexion = EstadoConexion.Desconectado,
    val estado: EstadoAforo? = null,
    val barras: List<Barra> = emptyList()
) {
    val conectado: Boolean get() = conexion is EstadoConexion.Conectado
}

class MonitorViewModel(repo: AforoRepository) : ViewModel() {

    val panel: StateFlow<Panel> = combine(repo.conexion, repo.estado, repo.historial) { conexion, estado, historial ->
        Panel(conexion, estado, historial.aBarras())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Panel())

    private fun List<Evento>.aBarras(): List<Barra> {
        val recientes = take(BARRAS).asReversed()
        val etiquetas = etiquetasDeTiempo(recientes.map { it.hora })
        return recientes.mapIndexed { i, evento -> Barra(evento.id, etiquetas[i], evento.personas) }
    }

    companion object {
        private const val BARRAS = 8

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { MonitorViewModel(container.aforoRepository) }
        }
    }
}
