package com.archplan.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import com.archplan.data.model.RoomData
import com.archplan.ui.theme.AmberAccent
import com.archplan.ui.theme.BlueprintBg
import com.archplan.ui.theme.BlueprintGrid
import com.archplan.ui.theme.BlueprintLabel
import com.archplan.ui.theme.BlueprintLine
import kotlin.math.max
import kotlin.math.min

/**
 * Blueprint-style canvas composable that draws the house plan with room
 * rectangles, labels, grid lines, compass, and scale bar.
 *
 * Supports pinch-to-zoom, pan, and tap-to-select rooms.
 */
@Composable
fun BlueprintCanvas(
    rooms: List<RoomData>,
    houseWidth: Float,
    houseHeight: Float,
    selectedRoomIndex: Int,
    onRoomSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Zoom and pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Animation progress for room drawing (0 → 1)
    var drawProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = drawProgress,
        animationSpec = tween(durationMillis = 1500),
        label = "drawProgress"
    )

    // Start animation
    LaunchedEffect(rooms) {
        drawProgress = 1f
    }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.3f, 3f)
        offsetX += panChange.x
        offsetY += panChange.y
    }

    Box(
        modifier = modifier
            .background(BlueprintBg)
            .transformable(state = transformState)
            .pointerInput(rooms) {
                detectTapGestures { tapOffset ->
                    // Convert tap to room coordinates
                    val viewW = size.width.toFloat()
                    val viewH = size.height.toFloat()
                    val padding = 20f
                    val availW = viewW - padding * 2
                    val availH = viewH - padding * 2
                    val drawScale = min(availW / max(houseWidth, 1f), availH / max(houseHeight, 1f)) * scale
                    val baseScale = min(availW / max(houseWidth, 1f), availH / max(houseHeight, 1f))
                    val drawOffsetX = (viewW - houseWidth * baseScale) / 2f + offsetX
                    val drawOffsetY = (viewH - houseHeight * baseScale) / 2f + offsetY

                    for ((index, room) in rooms.withIndex()) {
                        val rx = drawOffsetX + room.x * baseScale * scale
                        val ry = drawOffsetY + room.y * baseScale * scale
                        val rw = room.width * baseScale * scale
                        val rh = room.height * baseScale * scale
                        if (tapOffset.x in rx..(rx + rw) && tapOffset.y in ry..(ry + rh)) {
                            onRoomSelected(index)
                            return@detectTapGestures
                        }
                    }
                    onRoomSelected(-1)
                }
            }
            .graphicsLayer(
                scaleX = 1f,
                scaleY = 1f,
                translationX = 0f,
                translationY = 0f
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val padding = 20f
            val availW = size.width - padding * 2
            val availH = size.height - padding * 2
            val baseScale = min(availW / max(houseWidth, 1f), availH / max(houseHeight, 1f))
            val drawOffsetX = (size.width - houseWidth * baseScale) / 2f + offsetX
            val drawOffsetY = (size.height - houseHeight * baseScale) / 2f + offsetY

            // Apply zoom scale
            val finalScale = baseScale * scale
            val finalOffsetX = drawOffsetX
            val finalOffsetY = drawOffsetY

            // ── 1. Draw blueprint grid ────────────────────────────────────
            drawBlueprintGrid(size.width, size.height, finalScale)

            // ── 2. Draw compound wall ─────────────────────────────────────
            val compoundPaint = Stroke(
                width = 3f * scale,
                pathEffect = null
            )
            drawRect(
                color = BlueprintLine,
                topLeft = Offset(finalOffsetX, finalOffsetY),
                size = androidx.compose.ui.geometry.Size(
                    houseWidth * finalScale,
                    houseHeight * finalScale
                ),
                style = compoundPaint
            )

            // ── 3. Draw rooms with animation ──────────────────────────────
            val visibleCount = (rooms.size * animatedProgress).toInt().coerceAtLeast(0)
            for (i in 0 until visibleCount) {
                val room = rooms[i]
                val roomAnimProgress = ((animatedProgress * rooms.size) - i).coerceIn(0f, 1f)

                val rx = finalOffsetX + room.x * finalScale
                val ry = finalOffsetY + room.y * finalScale
                val rw = room.width * finalScale * roomAnimProgress
                val rh = room.height * finalScale * roomAnimProgress

                if (rw <= 0 || rh <= 0) continue

                val roomColor = Color(room.colorArgb)
                val isSelected = i == selectedRoomIndex

                // Fill
                drawRect(
                    color = roomColor.copy(alpha = 0.3f),
                    topLeft = Offset(rx, ry),
                    size = androidx.compose.ui.geometry.Size(rw, rh)
                )

                // Border
                drawRect(
                    color = if (isSelected) AmberAccent else BlueprintLine,
                    topLeft = Offset(rx, ry),
                    size = androidx.compose.ui.geometry.Size(rw, rh),
                    style = Stroke(
                        width = if (isSelected) 3f * scale else 2f * scale,
                        pathEffect = if (isSelected) PathEffect.dashPathEffect(
                            floatArrayOf(8f, 4f), 0f
                        ) else null
                    )
                )

                // Room name label
                drawContext.canvas.nativeCanvas.apply {
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = min(28f * scale, 28f)
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    val name = room.name
                    val dim = "${room.width.toInt()}x${room.height.toInt()} ft"
                    drawText(name, rx + rw / 2f, ry + rh / 2f - 8f * scale, textPaint)
                    textPaint.color = android.graphics.Color.parseColor("#4A8CFF")
                    textPaint.textSize = min(20f * scale, 20f)
                    drawText(dim, rx + rw / 2f, ry + rh / 2f + 20f * scale, textPaint)
                }

                // Door indicator (arc symbol)
                drawDoorIndicator(rx, ry, rw, rh, finalScale)

                // Window indicators
                drawWindowIndicators(rx, ry, rw, rh, finalScale)
            }

            // ── 4. Compass Rose ───────────────────────────────────────────
            drawCompassRose(size.width - 60f * scale, 60f * scale, 30f * scale)

            // ── 5. Scale Bar ──────────────────────────────────────────────
            drawScaleBar(padding, size.height - padding, finalScale)
        }
    }
}

/**
 * Draws the blueprint-style grid background.
 */
private fun DrawScope.drawBlueprintGrid(
    width: Float,
    height: Float,
    scale: Float
) {
    val gridSpacing = 30f * scale
    val gridColor = BlueprintGrid

    var x = 0f
    while (x < width) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = 0.5f)
        x += gridSpacing
    }
    var y = 0f
    while (y < height) {
        drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 0.5f)
        y += gridSpacing
    }
}

/**
 * Draws a compass rose (north arrow) in the top-right corner.
 */
private fun DrawScope.drawCompassRose(
    cx: Float,
    cy: Float,
    size: Float
) {
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#4A8CFF")
        textSize = size * 0.6f
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }

    // N arrow
    drawLine(
        BlueprintLabel,
        Offset(cx, cy + size),
        Offset(cx, cy - size),
        strokeWidth = 2f
    )
    // Arrow head
    val arrowPath = Path().apply {
        moveTo(cx, cy - size * 0.7f)
        lineTo(cx - size * 0.25f, cy - size * 0.2f)
        lineTo(cx + size * 0.25f, cy - size * 0.2f)
        close()
    }
    drawPath(arrowPath, color = BlueprintLabel)

    drawContext.canvas.nativeCanvas.drawText("N", cx, cy - size - 5f, paint)
}

/**
 * Draws a scale bar at the bottom of the canvas.
 */
private fun DrawScope.drawScaleBar(
    x: Float,
    y: Float,
    scale: Float
) {
    val barLength = 100f * scale
    val label = "1 cm = 5 ft"

    drawLine(BlueprintLabel, Offset(x, y), Offset(x + barLength, y), strokeWidth = 2f)
    drawLine(BlueprintLabel, Offset(x, y - 5f), Offset(x, y + 5f), strokeWidth = 2f)
    drawLine(BlueprintLabel, Offset(x + barLength, y - 5f), Offset(x + barLength, y + 5f), strokeWidth = 2f)

    drawContext.canvas.nativeCanvas.apply {
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#4A8CFF")
            textSize = 24f
            textAlign = android.graphics.Paint.Align.LEFT
            isAntiAlias = true
        }
        drawText(label, x + barLength / 2f, y - 10f, textPaint.apply { textAlign = android.graphics.Paint.Align.CENTER })
    }
}

/**
 * Draws a door indicator arc on the room edge closest to the entrance.
 */
private fun DrawScope.drawDoorIndicator(
    rx: Float, ry: Float, rw: Float, rh: Float, scale: Float
) {
    val doorSize = min(rw, rh) * 0.15f
    if (doorSize < 8f) return

    // Draw door on the bottom wall (simplified)
    val doorX = rx + rw * 0.3f
    val doorY = ry + rh

    drawArc(
        color = BlueprintLabel,
        topLeft = Offset(doorX - doorSize, doorY - doorSize),
        size = androidx.compose.ui.geometry.Size(doorSize * 2, doorSize * 2),
        startAngle = 0f,
        sweepAngle = 90f,
        useCenter = false,
        style = Stroke(width = 1.5f * scale)
    )
    drawLine(
        BlueprintLabel,
        Offset(doorX, doorY),
        Offset(doorX + doorSize, doorY),
        strokeWidth = 1.5f * scale
    )
}

/**
 * Draws window indicators as parallel lines on walls.
 */
private fun DrawScope.drawWindowIndicators(
    rx: Float, ry: Float, rw: Float, rh: Float, scale: Float
) {
    val winSize = min(rw, rh) * 0.2f
    if (winSize < 10f) return

    // Window on right wall
    val winY = ry + rh * 0.3f
    drawLine(
        BlueprintLabel,
        Offset(rx + rw, winY),
        Offset(rx + rw, winY + winSize),
        strokeWidth = 2f * scale
    )
    drawLine(
        BlueprintLabel,
        Offset(rx + rw + 4f * scale, winY),
        Offset(rx + rw + 4f * scale, winY + winSize),
        strokeWidth = 2f * scale
    )

    // Window on top wall
    val winX = rx + rw * 0.5f
    drawLine(
        BlueprintLabel,
        Offset(winX, ry),
        Offset(winX + winSize, ry),
        strokeWidth = 2f * scale
    )
    drawLine(
        BlueprintLabel,
        Offset(winX, ry - 4f * scale),
        Offset(winX + winSize, ry - 4f * scale),
        strokeWidth = 2f * scale
    )
}
