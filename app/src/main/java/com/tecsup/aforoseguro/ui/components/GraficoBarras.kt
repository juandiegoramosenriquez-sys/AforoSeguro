package com.tecsup.aforoseguro.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tecsup.aforoseguro.ui.theme.Borde
import com.tecsup.aforoseguro.ui.theme.Indigo
import com.tecsup.aforoseguro.ui.theme.IndigoClaro
import com.tecsup.aforoseguro.ui.theme.IndigoSuave
import com.tecsup.aforoseguro.ui.theme.Peligro

data class Barra(val id: Long, val etiqueta: String, val valor: Int)

private val DegradadoBarra = Brush.verticalGradient(listOf(IndigoClaro, Indigo))
private val DegradadoBarraTenue = Brush.verticalGradient(listOf(IndigoSuave, IndigoClaro.copy(alpha = 0.55f)))

@Composable
fun GraficoBarras(
    barras: List<Barra>,
    limite: Int,
    modifier: Modifier = Modifier
) {
    val tope = maxOf(limite, barras.maxOfOrNull { it.valor } ?: 0, 1)
    val fraccionLimite = limite.toFloat() / tope

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .drawBehind {
                    val guion = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    listOf(0f, 0.5f, 1f).forEach { f ->
                        val y = size.height * (1 - f)
                        drawLine(Borde, Offset(0f, y), Offset(size.width, y), strokeWidth = 2f, pathEffect = guion)
                    }
                    if (limite > 0) {
                        val y = size.height * (1 - fraccionLimite)
                        drawLine(Peligro.copy(alpha = 0.6f), Offset(0f, y), Offset(size.width, y), strokeWidth = 3f, pathEffect = guion)
                    }
                }
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                barras.forEachIndexed { indice, barra ->
                    key(barra.id) {
                        val altura by animateFloatAsState(
                            targetValue = (barra.valor.toFloat() / tope).coerceIn(0.03f, 1f),
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                            label = "altura"
                        )
                        val ultima = indice == barras.lastIndex
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (ultima) {
                                Text(
                                    "${barra.valor}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Indigo,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(altura)
                                    .background(
                                        if (ultima) DegradadoBarra else DegradadoBarraTenue,
                                        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 3.dp, bottomEnd = 3.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            barras.forEachIndexed { indice, barra ->
                Text(
                    barra.etiqueta,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    fontWeight = if (indice == barras.lastIndex) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
