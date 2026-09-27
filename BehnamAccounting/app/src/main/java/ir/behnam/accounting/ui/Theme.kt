package ir.behnam.accounting.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy = Color(0xFF071426)
val NavyCard = Color(0xFF0D213A)
val Emerald = Color(0xFF42D99B)
val Coral = Color(0xFFFF7B6C)

private val BehnamColors = darkColorScheme(
    primary = Emerald, secondary = Color(0xFF4F9DFF), tertiary = Coral,
    background = Navy, surface = NavyCard, surfaceVariant = Color(0xFF132C49),
    onPrimary = Navy, onBackground = Color(0xFFF2F6FF), onSurface = Color(0xFFF2F6FF), onSurfaceVariant = Color(0xFFB9C6D8), error = Coral
)

@Composable fun BehnamTheme(content: @Composable () -> Unit) = MaterialTheme(colorScheme = BehnamColors, typography = Typography(), content = content)
