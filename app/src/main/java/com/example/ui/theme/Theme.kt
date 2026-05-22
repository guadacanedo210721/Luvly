package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Custom Glassmorphic Modifier extension for UI components
fun Modifier.glassmorphicBorder(
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.dp,
    borderColor: Color = GlassBorder
): Modifier {
    return this.border(borderWidth, borderColor, shape)
}

private val LightColorScheme = lightColorScheme(
    primary = RomanticPinkPrimary,
    onPrimary = OnPrimaryLight,
    primaryContainer = GlassSurfaceBrighter,
    onPrimaryContainer = Color.White,
    secondary = RomanticPinkSecondary,
    onSecondary = Color.White,
    secondaryContainer = GlassSurface,
    tertiary = CoralPink,
    onTertiary = Color.White,
    background = FrostedBackgroundDark,
    onBackground = Color.White,
    surface = GlassSurface,
    onSurface = Color.White,
    surfaceVariant = GlassSurfaceBrighter,
    onSurfaceVariant = Color.White,
    outline = GlassBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = RomanticPinkPrimary,
    onPrimary = OnPrimaryLight,
    primaryContainer = GlassSurfaceBrighter,
    onPrimaryContainer = Color.White,
    secondary = RomanticPinkSecondary,
    onSecondary = Color.White,
    background = FrostedBackgroundDark,
    onBackground = Color.White,
    surface = GlassSurface,
    onSurface = Color.White,
    surfaceVariant = GlassSurfaceBrighter,
    onSurfaceVariant = Color.White,
    outline = GlassBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve our beautiful frosted glow theme custom styling
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        // App-wide Frosted Glass ambient glows background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Solid dark backplate
                    drawRect(FrostedBackgroundDark)

                    if (size.width > 0f && size.height > 0f) {
                        // Glow blob styling matching HTML template colors/opacities
                        val purpleGlow = Color(0x666750A4)      // 40% `#6750A4` blur glow
                        val lavenderGlow = Color(0x4DD0BCFF)    // 30% `#D0BCFF` blur glow
                        val deepPurpGlow = Color(0x80381E72)    // 50% `#381E72` blur glow

                        // 1. Top-Left glowing blob: width ~70%, height ~50%
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(purpleGlow, Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(x = -size.width * 0.1f, y = -size.height * 0.1f),
                                radius = size.width * 0.8f
                            ),
                            center = androidx.compose.ui.geometry.Offset(x = -size.width * 0.1f, y = -size.height * 0.1f),
                            radius = size.width * 0.8f
                        )

                        // 2. Bottom-Right glowing blob: width ~60%, height ~40%
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(lavenderGlow, Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(x = size.width * 1.1f, y = size.height * 0.9f),
                                radius = size.width * 0.75f
                            ),
                            center = androidx.compose.ui.geometry.Offset(x = size.width * 1.1f, y = size.height * 0.9f),
                            radius = size.width * 0.75f
                        )

                        // 3. Middle-Right deep purple glowing blob: width ~40%, height ~30%
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(deepPurpGlow, Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(x = size.width * 0.9f, y = size.height * 0.45f),
                                radius = size.width * 0.65f
                            ),
                            center = androidx.compose.ui.geometry.Offset(x = size.width * 0.9f, y = size.height * 0.45f),
                            radius = size.width * 0.65f
                        )
                    }
                }
        ) {
            content()
        }
    }
}

