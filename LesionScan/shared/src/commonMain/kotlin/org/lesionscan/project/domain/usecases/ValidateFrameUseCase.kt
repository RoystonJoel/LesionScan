package org.lesionscan.project.domain.usecases

import org.lesionscan.project.domain.entities.FrameQuality

/**
 * Business logic for validating camera frame quality.
 *
 * Per the Milestone 1 design:
 * - Lighting: Measure brightness to avoid underexposure/overexposure
 * - Focus: Compute Laplacian sharpness to detect blur
 * - Distance: Estimate lesion size in frame (should occupy 20-60% of frame)
 */
class ValidateFrameUseCase {

    // Thresholds for optimal conditions
    companion object {
        const val LIGHTING_MIN = 0.25f          // Too dark below this
        const val LIGHTING_MAX = 0.95f          // Overexposed above this
        const val SHARPNESS_THRESHOLD = 0.6f    // Laplacian score
        const val LESION_AREA_MIN = 0.15f       // Lesion should fill 15%+ of frame
        const val LESION_AREA_MAX = 0.65f       // But not more than 65% (too close)
    }

    /**
     * Validate a frame's quality metrics.
     *
     * @param lightingLevel Brightness 0.0-1.0 (computed from frame histogram)
     * @param sharpnessScore Laplacian variance normalized to 0.0-1.0
     * @param lesionAreaPercentage Detected lesion bounding box as % of frame
     * @return FrameQuality with pass/fail for each criterion
     */
    fun execute(
        lightingLevel: Float,
        sharpnessScore: Float,
        lesionAreaPercentage: Float
    ): FrameQuality {
        return FrameQuality(
            isLightingGood = lightingLevel in LIGHTING_MIN..LIGHTING_MAX,
            isFocusSharp = sharpnessScore >= SHARPNESS_THRESHOLD,
            isDistanceCorrect = lesionAreaPercentage in LESION_AREA_MIN..LESION_AREA_MAX,
            lightingLevel = lightingLevel,
            sharpnessScore = sharpnessScore,
            lesionAreaPercentage = lesionAreaPercentage
        )
    }
}