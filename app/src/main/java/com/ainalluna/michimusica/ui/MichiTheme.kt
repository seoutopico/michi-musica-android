package com.ainalluna.michimusica.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class MichiSkin { ROSE, MIDNIGHT }

private val RoseColors = lightColorScheme(
    primary = Color(0xFFCF5F8C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF8DDE7),
    onPrimaryContainer = Color(0xFF49334F),
    secondary = Color(0xFF746077),
    secondaryContainer = Color(0xFFF2E7EC),
    onSecondaryContainer = Color(0xFF493B49),
    background = Color(0xFFFFFAF7),
    surface = Color(0xFFFFFCFA),
    surfaceVariant = Color(0xFFF6EFEC),
    surfaceContainer = Color(0xFFFFF8F5),
    surfaceContainerHigh = Color(0xFFF9ECEF),
    onSurface = Color(0xFF403047),
    onSurfaceVariant = Color(0xFF776B75),
    outline = Color(0xFFDFD3D7),
)

private val MidnightColors = darkColorScheme(
    primary = Color(0xFFFF88AC),
    onPrimary = Color(0xFF541E2E),
    primaryContainer = Color(0xFF38253D),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = Color(0xFFAED6B8),
    secondaryContainer = Color(0xFF333B47),
    background = Color(0xFF090A1B),
    surface = Color(0xFF101022),
    surfaceVariant = Color(0xFF231C31),
    surfaceContainer = Color(0xFF121124),
    surfaceContainerHigh = Color(0xFF201B30),
    onSurface = Color(0xFFF8F5FA),
    onSurfaceVariant = Color(0xFFBEB0C2),
    outline = Color(0xFF594058),
)

private val MichiTypography = Typography(
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 23.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
)

@Composable
fun MichiTheme(skin: MichiSkin, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (skin == MichiSkin.ROSE) RoseColors else MidnightColors,
        typography = MichiTypography,
        content = content,
    )
}
