package br.edu.iftm.gestorpendencias.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import br.edu.iftm.gestorpendencias.util.Urgencia

private val Esquema = lightColorScheme(
    primary = Color(0xFF3F51B5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E3FF),
    secondary = Color(0xFF00897B),
    background = Color(0xFFFAFAFC),
    surface = Color.White,
    error = Color(0xFFC62828)
)

@Composable
fun PendenciasTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Esquema, content = content)
}

fun Urgencia.cor(): Color = when (this) {
    Urgencia.NO_PRAZO -> Color(0xFF2E7D32)
    Urgencia.PROXIMA -> Color(0xFFF9A825)
    Urgencia.ATRASADA -> Color(0xFFC62828)
    Urgencia.CONCLUIDA -> Color(0xFF757575)
}

fun Urgencia.fundo(): Color = when (this) {
    Urgencia.NO_PRAZO -> Color(0xFFC8E6C9)
    Urgencia.PROXIMA -> Color(0xFFFFF59D)
    Urgencia.ATRASADA -> Color(0xFFFFCDD2)
    Urgencia.CONCLUIDA -> Color(0xFFE0E0E0)
}
