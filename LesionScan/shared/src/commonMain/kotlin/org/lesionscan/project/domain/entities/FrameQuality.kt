package org.lesionscan.project.domain.entities


/**
 * Represents the quality metrics of a camera frame.
 * Used by FrameValidator to determine if conditions are optimal for ML inference.
 */
data class FrameQuality(
    val isLightingGood: Boolean,      // Brightness within acceptable range
    val isFocusSharp: Boolean,         // Image sharpness passes threshold
    val isDistanceCorrect: Boolean,    // Lesion occupies correct frame area
    val lightingLevel: Float,          // 0.0 - 1.0 (0 = dark, 1 = overexposed)
    val sharpnessScore: Float,         // 0.0 - 1.0 (higher = sharper)
    val lesionAreaPercentage: Float    // 0.0 - 1.0 (% of frame occupied by lesion)
) {
    /**
     * Returns true only if ALL conditions are met for inference.
     * Used to trigger auto-capture.
     */
    val isOptimal: Boolean
        get() = isLightingGood && isFocusSharp && isDistanceCorrect
}