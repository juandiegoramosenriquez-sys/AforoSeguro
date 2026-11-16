package com.tecsup.aforoseguro.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.aforoseguro.AppContainer
import com.tecsup.aforoseguro.data.AforoRepository
import com.tecsup.aforoseguro.domain.model.Evento
import com.tecsup.aforoseguro.domain.model.TipoEvento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class Filtro(val etiqueta: String, val tipo: TipoEvento?) {
    TODOS("Todos", null),
    ENTRADAS("Entradas", TipoEvento.ENTRADA),
    SALIDAS("Salidas", TipoEvento.SALIDA)
}

data class Historial(
    val eventos: List<Evento> = emptyList(),
    val filtro: Filtro = Filtro.TODOS,
    val entradas: Int = 0,
    val salidas: Int = 0
) {
    val total: Int get() = entradas + salidas
}

class HistorialViewModel(repo: AforoRepository) : ViewModel() {

    private val filtro = MutableStateFlow(Filtro.TODOS)

    val historial: StateFlow<Historial> = combine(repo.historial, filtro) { eventos, filtroActual ->
        val entradas = eventos.count { it.tipo == TipoEvento.ENTRADA }
        Historial(
            eventos = filtroActual.tipo?.let { tipo -> eventos.filter { it.tipo == tipo } } ?: eventos,
            filtro = filtroActual,
            entradas = entradas,
            salidas = eventos.size - entradas
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Historial())

    fun filtrar(nuevo: Filtro) {
        filtro.value = nuevo
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { HistorialViewModel(container.aforoRepository) }
        }
    }
}
