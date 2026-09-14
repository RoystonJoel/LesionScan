package org.lesionscan.project.presentation.components

import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Jetpack Compose wrapper for CameraX PreviewView.
 * Displays live camera feed.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    previewView: PreviewView
) {
    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}