package org.lesionscan.project.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.lesionscan.project.presentation.viewmodels.FrameQualityState

/**
 * Real-time guidance overlay for camera frame validation.
 *
 * Displays:
 * - Circular target (white dashed ring) showing optimal lesion placement
 * - Corner indicators (green ✓ / red ✗) for lighting, focus, distance
 * - Center status text
 */
@Composable
fun GuidanceOverlay(
    frameQualityState: FrameQualityState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2

            // Draw circular target (lesion should occupy this area)
            val targetRadius = 120.dp.toPx()

            // Outer circle (dashed white)
            drawCircle(
                color = Color.White,
                radius = targetRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2.dp.toPx())
            )

            // Inner circle (lighter, guidance only)
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = targetRadius * 0.7f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.dp.toPx())
            )

            // Corner indicator boxes
            val cornerSize = 40.dp.toPx()
            val cornerPadding = 20.dp.toPx()

            // Top-left: Lighting
            drawRoundRect(
                color = if (frameQualityState.isLighting) Color.Green else Color.Red,
                topLeft = Offset(cornerPadding, cornerPadding),
                size = Size(cornerSize, cornerSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )

            // Top-right: Focus
            drawRoundRect(
                color = if (frameQualityState.isFocus) Color.Green else Color.Red,
                topLeft = Offset(size.width - cornerSize - cornerPadding, cornerPadding),
                size = Size(cornerSize, cornerSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )

            // Bottom-left: Distance
            drawRoundRect(
                color = if (frameQualityState.isDistance) Color.Green else Color.Red,
                topLeft = Offset(cornerPadding, size.height - cornerSize - cornerPadding),
                size = Size(cornerSize, cornerSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )

            // Center circle (pulse if optimal)
            val pulseColor = if (frameQualityState.isOptimal) Color.Green else Color.Yellow
            val pulseAlpha = if (frameQualityState.isOptimal) 0.5f else 0.3f

            drawCircle(
                color = pulseColor.copy(alpha = pulseAlpha),
                radius = 8.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }

        // Status text overlay
        Text(
            text = when {
                !frameQualityState.isLighting -> "✗ Lighting too dark or bright"
                !frameQualityState.isFocus -> "✗ Camera is blurry"
                !frameQualityState.isDistance -> "✗ Adjust distance"
                frameQualityState.isOptimal -> "✓ Perfect! Ready to capture"
                else -> "Position lesion in circle"
            },
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
        )
    }
}