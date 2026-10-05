package org.lesionscan.project.presentation

data class FrameQualityState(
    val isLighting: Boolean = false,
    val isFocus: Boolean = false,
    val isDistance: Boolean = false,
    val isOptimal: Boolean = false,
    val lightingLevel: Float = 0f,
    val focusLevel: Float = 0f,
    val distanceLevel: Float = 0f
)