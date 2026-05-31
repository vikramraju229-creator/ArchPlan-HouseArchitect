package com.archplan.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archplan.ui.theme.BlueprintBg
import com.archplan.ui.theme.BlueprintGrid
import com.archplan.ui.theme.BlueprintLine
import kotlinx.coroutines.delay

/**
 * Animated splash screen with blueprint grid drawing animation.
 */
@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit
) {
    // Grid animation
    var gridProgress by remember { mutableFloatStateOf(0f) }
    val animatedGridProgress by animateFloatAsState(
        targetValue = gridProgress,
        animationSpec = tween(durationMillis = 1500),
        label = "gridProgress"
    )

    // Logo visibility
    var showLogo by remember { mutableStateOf(false) }
    val logoAlpha by animateFloatAsState(
        targetValue = if (showLogo) 1f else 0f,
        animationSpec = tween(800),
        label = "logoAlpha"
    )

    // Title typewriter
    val title = "ArchPlan"
    var visibleChars by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        gridProgress = 1f
        delay(800)
        showLogo = true
        // Typewriter effect
        for (i in 1..title.length) {
            visibleChars = i
            delay(80)
        }
        delay(1200)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueprintBg),
        contentAlignment = Alignment.Center
    ) {
        // Animated blueprint grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 40f
            val gridColor = BlueprintGrid

            // Draw grid lines from center outward
            val centerX = size.width / 2
            val centerY = size.height / 2
            val maxDim = maxOf(size.width, size.height) * 0.5f
            val drawExtent = maxDim * animatedGridProgress

            // Vertical lines
            var x = centerX
            while (x - centerX <= drawExtent) {
                drawLine(gridColor, Offset(x, centerY - drawExtent), Offset(x, centerY + drawExtent), strokeWidth = 0.5f)
                drawLine(gridColor, Offset(centerX * 2 - x, centerY - drawExtent), Offset(centerX * 2 - x, centerY + drawExtent), strokeWidth = 0.5f)
                x += gridSpacing
            }

            // Horizontal lines
            var y = centerY
            while (y - centerY <= drawExtent) {
                drawLine(gridColor, Offset(centerX - drawExtent, y), Offset(centerX + drawExtent, y), strokeWidth = 0.5f)
                drawLine(gridColor, Offset(centerX - drawExtent, centerY * 2 - y), Offset(centerX + drawExtent, centerY * 2 - y), strokeWidth = 0.5f)
                y += gridSpacing
            }

            // Draw blueprint-style hexagon shape
            if (animatedGridProgress > 0.5f) {
                val hexProgress = (animatedGridProgress - 0.5f) * 2f
                val hexSize = minOf(size.width, size.height) * 0.2f * hexProgress
                val hexPath = Path().apply {
                    moveTo(centerX, centerY - hexSize)
                    for (i in 1..6) {
                        val angle = Math.toRadians((i * 60.0).toDouble())
                        lineTo(
                            (centerX + hexSize * kotlin.math.sin(angle).toFloat()),
                            (centerY - hexSize * kotlin.math.cos(angle).toFloat())
                        )
                    }
                    close()
                }
                drawPath(
                    hexPath,
                    color = BlueprintLine.copy(alpha = 0.3f),
                    style = Stroke(
                        width = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f)
                    )
                )
            }
        }

        // Logo and title
        Column(
            modifier = Modifier.alpha(logoAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Blueprint icon
            Canvas(modifier = Modifier.size(80.dp)) {
                val cX = size.width / 2
                val cY = size.height / 2
                val r = minOf(size.width, size.height) * 0.35f

                // Triangle (roof)
                val roof = Path().apply {
                    moveTo(cX, cY - r)
                    lineTo(cX - r, cY + r * 0.3f)
                    lineTo(cX + r, cY + r * 0.3f)
                    close()
                }
                drawPath(roof, color = BlueprintLine, style = Stroke(width = 3f))

                // Square (base)
                val base = Path().apply {
                    addRect(
                        cX - r * 0.6f, cY + r * 0.3f,
                        cX + r * 0.6f, cY + r * 1.2f
                    )
                }
                drawPath(base, color = BlueprintLine, style = Stroke(width = 2f))

                // Door
                drawLine(BlueprintLine, Offset(cX, cY + r * 0.7f), Offset(cX, cY + r * 1.1f), strokeWidth = 2f)
                drawLine(BlueprintLine, Offset(cX - r * 0.15f, cY + r * 0.7f), Offset(cX - r * 0.15f, cY + r * 1.1f), strokeWidth = 2f)
                drawLine(BlueprintLine, Offset(cX - r * 0.15f, cY + r * 1.1f), Offset(cX + r * 0.15f, cY + r * 1.1f), strokeWidth = 2f)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Typewriter title
            Text(
                text = title.take(visibleChars),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Genius House Architect",
                style = MaterialTheme.typography.titleMedium,
                color = BlueprintLine
            )
        }
    }
}
