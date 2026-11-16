package com.tecsup.aforoseguro.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tecsup.aforoseguro.AppContainer
import com.tecsup.aforoseguro.R
import com.tecsup.aforoseguro.ui.alerta.AlertaViewModel
import com.tecsup.aforoseguro.ui.conexion.Conector
import com.tecsup.aforoseguro.ui.conexion.ConexionViewModel
import com.tecsup.aforoseguro.ui.configuracion.ConfiguracionScreen
import com.tecsup.aforoseguro.ui.configuracion.ConfiguracionViewModel
import com.tecsup.aforoseguro.ui.historial.HistorialScreen
import com.tecsup.aforoseguro.ui.historial.HistorialViewModel
import com.tecsup.aforoseguro.ui.monitor.MonitorScreen
import com.tecsup.aforoseguro.ui.monitor.MonitorViewModel

private data class Pestana(
    val ruta: String,
    @param:StringRes val titulo: Int,
    val icono: ImageVector,
    val iconoActivo: ImageVector
)

private val pestanas = listOf(
    Pestana("inicio", R.string.nav_inicio, Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
    Pestana("historial", R.string.nav_historial, Icons.Outlined.History, Icons.Filled.History),
    Pestana("ajustes", R.string.nav_ajustes, Icons.Outlined.Settings, Icons.Filled.Settings)
)

@Composable
fun AppNav(container: AppContainer) {
    viewModel<AlertaViewModel>(factory = AlertaViewModel.factory(container))
    val conexion: ConexionViewModel = viewModel(factory = ConexionViewModel.factory(container))
    val navController = rememberNavController()
    val entrada by navController.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route

    Conector(conexion) { iniciarConexion ->
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = { BarraInferior(rutaActual) { irA(navController, it) } }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "inicio",
                modifier = Modifier.padding(padding)
            ) {
                composable("inicio") {
                    MonitorScreen(
                        viewModel = viewModel(factory = MonitorViewModel.factory(container)),
                        onConectar = iniciarConexion,
                        onDemo = conexion::iniciarDemo
                    )
                }
                composable("historial") {
                    HistorialScreen(viewModel(factory = HistorialViewModel.factory(container)))
                }
                composable("ajustes") {
                    ConfiguracionScreen(
                        viewModel = viewModel(factory = ConfiguracionViewModel.factory(container)),
                        onConectar = iniciarConexion,
                        onDemo = conexion::iniciarDemo,
                        onDesconectar = conexion::desconectar
                    )
                }
            }
        }
    }
}

@Composable
private fun BarraInferior(rutaActual: String?, onSeleccionar: (String) -> Unit) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            pestanas.forEach { pestana ->
                val activa = rutaActual == pestana.ruta
                NavigationBarItem(
                    selected = activa,
                    onClick = { onSeleccionar(pestana.ruta) },
                    icon = { Icon(if (activa) pestana.iconoActivo else pestana.icono, contentDescription = null) },
                    label = { Text(stringResource(pestana.titulo)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

private fun irA(navController: NavHostController, ruta: String) {
    navController.navigate(ruta) {
        popUpTo("inicio") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
