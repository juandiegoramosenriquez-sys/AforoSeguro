package com.tecsup.aforoseguro.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tecsup.aforoseguro.domain.model.NivelAforo

private val Colores = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = IndigoSuave,
    onPrimaryContainer = IndigoProfundo,
    secondary = IndigoClaro,
    onSecondary = Color.White,
    secondaryContainer = IndigoSuave,
    onSecondaryContainer = IndigoProfundo,
    background = Fondo,
    onBackground = Tinta,
    surface = Superficie,
    onSurface = Tinta,
    surfaceVariant = Fondo,
    onSurfaceVariant = TintaSuave,
    surfaceContainerLowest = Superficie,
    surfaceContainerLow = Superficie,
    surfaceContainer = Superficie,
    surfaceContainerHigh = Superficie,
    surfaceContainerHighest = Fondo,
    outline = Borde,
    outlineVariant = Borde,
    error = Peligro,
    onError = Color.White,
    errorContainer = PeligroSuave,
    onErrorContainer = Peligro
)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AforoSeguroTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colores, typography = Tipografia, shapes = Formas, content = content)
}

data class PaletaNivel(
    val principal: Color,
    val suave: Color,
    val inicio: Color,
    val fin: Color,
    val etiqueta: String
) {
    val degradado: Brush get() = Brush.linearGradient(listOf(inicio, fin))
}

fun NivelAforo.paleta(): PaletaNivel = when (this) {
    NivelAforo.SEGURO -> PaletaNivel(Exito, ExitoSuave, Indigo, IndigoClaro, "Seguro")
    NivelAforo.ALERTA -> PaletaNivel(Aviso, AvisoSuave, Aviso, AvisoClaro, "Alerta")
    NivelAforo.LLENO -> PaletaNivel(Peligro, PeligroSuave, Peligro, PeligroClaro, "Lleno")
}
