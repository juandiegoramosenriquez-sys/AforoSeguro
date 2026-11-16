package com.tecsup.aforoseguro.ui.alerta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.aforoseguro.AppContainer
import com.tecsup.aforoseguro.data.AforoRepository
import com.tecsup.aforoseguro.domain.model.NivelAforo
import com.tecsup.aforoseguro.notification.Notificador
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class AlertaViewModel(
    private val repo: AforoRepository,
    private val notificador: Notificador
) : ViewModel() {

    private var nivelAnterior: NivelAforo? = null

    init {
        viewModelScope.launch {
            repo.estado
                .filterNotNull()
                .collect { estado ->
                    val nivel = estado.nivel
                    if (nivel == NivelAforo.LLENO && nivelAnterior != null && nivelAnterior != NivelAforo.LLENO) {
                        notificador.avisarAforoLleno(estado.personas, estado.maximo)
                    }
                    nivelAnterior = nivel
                }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { AlertaViewModel(container.aforoRepository, container.notificador) }
        }
    }
}
