package com.pawar47.issuerank.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** iOS system colors (light + dark), plus the translucent "glass" material used for floating chrome. */
@Immutable
class IosColors(
    val isDark: Boolean,
    val background: Color,
    val card: Color,
    val sheetBackground: Color,
    val sheetCard: Color,
    val label: Color,
    val secondaryLabel: Color,
    val tertiaryLabel: Color,
    val separator: Color,
    val fill: Color,
    val segmentSelected: Color,
    val switchOff: Color,
    val blue: Color,
    val green: Color,
    val orange: Color,
    val red: Color,
    val glass: Color,
    val glassBorder: Color
)

val LightIosColors = IosColors(
    isDark = false,
    background = Color(0xFFF2F2F7),
    card = Color(0xFFFFFFFF),
    sheetBackground = Color(0xFFF2F2F7),
    sheetCard = Color(0xFFFFFFFF),
    label = Color(0xFF000000),
    secondaryLabel = Color(0x993C3C43),
    tertiaryLabel = Color(0x4D3C3C43),
    separator = Color(0x4A3C3C43),
    fill = Color(0x1F787880),
    segmentSelected = Color(0xFFFFFFFF),
    switchOff = Color(0x29787880),
    blue = Color(0xFF007AFF),
    green = Color(0xFF34C759),
    orange = Color(0xFFFF9500),
    red = Color(0xFFFF3B30),
    glass = Color(0xDBFFFFFF),
    glassBorder = Color(0x14000000)
)

val DarkIosColors = IosColors(
    isDark = true,
    background = Color(0xFF000000),
    card = Color(0xFF1C1C1E),
    sheetBackground = Color(0xFF1C1C1E),
    sheetCard = Color(0xFF2C2C2E),
    label = Color(0xFFFFFFFF),
    secondaryLabel = Color(0x99EBEBF5),
    tertiaryLabel = Color(0x4DEBEBF5),
    separator = Color(0x59545458),
    fill = Color(0x5C787880),
    segmentSelected = Color(0xFF636366),
    switchOff = Color(0x5C787880),
    blue = Color(0xFF0A84FF),
    green = Color(0xFF30D158),
    orange = Color(0xFFFF9F0A),
    red = Color(0xFFFF453A),
    glass = Color(0xDB2C2C2E),
    glassBorder = Color(0x1FFFFFFF)
)

val LocalIosColors = staticCompositionLocalOf { LightIosColors }

object Ios {
    val colors: IosColors
        @Composable
        @ReadOnlyComposable
        get() = LocalIosColors.current
}

/** iOS text styles (Dynamic Type "large" sizes). */
object IosType {
    val largeTitle = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
    val title = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
    val headline = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    val body = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Normal)
    val subhead = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal)
    val footnote = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal)
    val caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    val tabLabel = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium)
}

@Composable
fun IosTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkIosColors else LightIosColors
    CompositionLocalProvider(LocalIosColors provides colors, content = content)
}
