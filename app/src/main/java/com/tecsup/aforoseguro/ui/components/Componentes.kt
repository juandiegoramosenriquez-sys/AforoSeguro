package com.tecsup.aforoseguro.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tecsup.aforoseguro.ui.theme.Borde
import com.tecsup.aforoseguro.ui.theme.Indigo
import com.tecsup.aforoseguro.ui.theme.IndigoSuave
import com.tecsup.aforoseguro.ui.theme.Superficie

val FormaTarjeta = RoundedCornerShape(22.dp)

fun Modifier.estiloTarjeta(fondo: Color = Superficie): Modifier = this
    .shadow(10.dp, FormaTarjeta, ambientColor = Indigo.copy(alpha = 0.06f), spotColor = Indigo.copy(alpha = 0.10f))
    .clip(FormaTarjeta)
    .background(fondo)
    .border(1.dp, Borde, FormaTarjeta)

@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    relleno: Dp = 18.dp,
    espacio: Dp = 12.dp,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .estiloTarjeta()
            .padding(relleno),
        verticalArrangement = Arrangement.spacedBy(espacio),
        content = contenido
    )
}

@Composable
fun Insignia(
    icono: ImageVector,
    color: Color = Indigo,
    fondo: Color = IndigoSuave,
    tamano: Dp = 44.dp
) {
    Box(
        modifier = Modifier
            .size(tamano)
            .background(fondo, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(tamano * 0.5f))
    }
}

@Composable
fun Pildora(
    texto: String,
    color: Color,
    fondo: Color,
    modifier: Modifier = Modifier,
    conPunto: Boolean = true
) {
    Row(
        modifier = modifier
            .background(fondo, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (conPunto) {
            Box(
                Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(texto, color = color, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun ContadorAnimado(valor: Int, estilo: TextStyle, color: Color = Color.Unspecified) {
    AnimatedContent(
        targetState = valor,
        transitionSpec = {
            val sube = targetState > initialState
            (slideInVertically { if (sube) it else -it } + fadeIn())
                .togetherWith(slideOutVertically { if (sube) -it else it } + fadeOut())
                .using(SizeTransform(clip = true))
        },
        label = "contador"
    ) { numero ->
        Text("$numero", style = estilo, color = color)
    }
}

@Composable
fun Encabezado(
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier,
    antetitulo: String? = null,
    accion: @Composable RowScope.() -> Unit = {}
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            antetitulo?.let {
                Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Text(titulo, style = MaterialTheme.typography.headlineMedium)
            Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        accion()
    }
}

@Composable
fun Banner(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    color: Color,
    fondo: Color,
    modifier: Modifier = Modifier,
    accion: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(fondo)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Insignia(icono, color = Color.White, fondo = color, tamano = 36.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall, color = color)
            Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        accion()
    }
}

@Composable
fun EstadoVacio(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    modifier: Modifier = Modifier,
    accion: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Insignia(icono, tamano = 72.dp)
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            detalle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        accion()
    }
}
