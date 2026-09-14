package org.lesionscan.project.presentation.screens

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import org.lesionscan.project.presentation.components.CameraPreview
import org.lesionscan.project.presentation.components.GuidanceOverlay
import org.lesionscan.project.presentation.viewmodels.CameraViewModel

@Composable
fun CameraScreen(lifecycleOwner: LifecycleOwner) {
    val context = LocalContext.current

    val viewModel = remember {
        CameraViewModel(context, lifecycleOwner)
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(Unit) {
        viewModel.startCamera(previewView)
        onDispose {
            viewModel.stopCamera()
        }
    }

    val frameQualityState = viewModel.frameQualityState.collectAsState()
    val isFlashOn = viewModel.isFlashOn.collectAsState()
    val state = frameQualityState.value

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Camera preview
        CameraPreview(
            previewView = previewView,
            modifier = Modifier.fillMaxSize()
        )

        // Guidance overlay
        GuidanceOverlay(
            frameQualityState = state,
            modifier = Modifier.fillMaxSize()
        )

        // Flash toggle button (top-right)
        OutlinedButton(
            onClick = { viewModel.toggleFlash() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(
                if (isFlashOn.value) "🔦 Flash On" else "🔦 Flash Off",
                fontSize = 12.sp,
                color = if (isFlashOn.value) Color.Yellow else Color.Gray
            )
        }

        // Bottom control panel
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(16.dp)
        ) {
            Text("Frame Quality", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // Lighting progress
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Lighting", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.width(60.dp))
                LinearProgressIndicator(
                    progress = state.lightingLevel,
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp),
                    color = if (state.isLighting) Color.Green else Color.Red
                )
                Text(
                    String.format("%.0f%%", state.lightingLevel * 100),
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 8.dp).width(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Focus progress
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Focus", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.width(60.dp))
                LinearProgressIndicator(
                    progress = state.focusLevel,
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp),
                    color = if (state.isFocus) Color.Green else Color.Red
                )
                Text(
                    String.format("%.0f%%", state.focusLevel * 100),
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 8.dp).width(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Distance progress
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Distance", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.width(60.dp))
                LinearProgressIndicator(
                    progress = state.distanceLevel,
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp),
                    color = if (state.isDistance) Color.Green else Color.Red
                )
                Text(
                    String.format("%.0f%%", state.distanceLevel * 100),
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 8.dp).width(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Capture button
            Button(
                onClick = { /* TODO: Trigger capture + inference */ },
                enabled = state.isOptimal,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.8f)
            ) {
                Text(
                    if (state.isOptimal) "✓ Capture" else "⟳ Adjust Camera",
                    fontSize = 14.sp
                )
            }
        }
    }
}