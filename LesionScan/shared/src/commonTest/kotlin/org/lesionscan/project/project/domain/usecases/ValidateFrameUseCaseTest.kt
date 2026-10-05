package org.lesionscan.project.project.domain.usecases

import org.lesionscan.project.domain.usecases.ValidateFrameUseCase
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ValidateFrameUseCaseTest {

    private val useCase = ValidateFrameUseCase()

    @Test
    fun testOptimalFrameConditions() {
        val quality = useCase.execute(
            lightingLevel = 0.5f,      // Good lighting
            sharpnessScore = 0.75f,    // Sharp
            lesionAreaPercentage = 0.35f  // Good distance
        )

        assertTrue(quality.isOptimal, "Frame with all metrics green should be optimal")
        assertTrue(quality.isLightingGood)
        assertTrue(quality.isFocusSharp)
        assertTrue(quality.isDistanceCorrect)
    }

    @Test
    fun testDarkFrameRejectsCapture() {
        val quality = useCase.execute(
            lightingLevel = 0.10f,     // Too dark
            sharpnessScore = 0.75f,
            lesionAreaPercentage = 0.35f
        )

        assertFalse(quality.isLightingGood, "Dark frame should fail lighting check")
        assertFalse(quality.isOptimal, "Frame should not be optimal")
    }

    @Test
    fun testOverexposedFrameRejectsCapture() {
        val quality = useCase.execute(
            lightingLevel = 0.98f,     // Too bright
            sharpnessScore = 0.75f,
            lesionAreaPercentage = 0.35f
        )

        assertFalse(quality.isLightingGood, "Overexposed frame should fail lighting check")
        assertFalse(quality.isOptimal)
    }

    @Test
    fun testBlurryFrameRejectsCapture() {
        val quality = useCase.execute(
            lightingLevel = 0.5f,
            sharpnessScore = 0.40f,    // Too blurry
            lesionAreaPercentage = 0.35f
        )

        assertFalse(quality.isFocusSharp, "Blurry frame should fail focus check")
        assertFalse(quality.isOptimal)
    }

    @Test
    fun testLesionTooClosRejectsCapture() {
        val quality = useCase.execute(
            lightingLevel = 0.5f,
            sharpnessScore = 0.75f,
            lesionAreaPercentage = 0.80f  // Too close (fills 80% of frame)
        )

        assertFalse(quality.isDistanceCorrect, "Lesion too close should fail distance check")
        assertFalse(quality.isOptimal)
    }

    @Test
    fun testLesionTooFarRejectsCapture() {
        val quality = useCase.execute(
            lightingLevel = 0.5f,
            sharpnessScore = 0.75f,
            lesionAreaPercentage = 0.05f  // Too far (fills only 5% of frame)
        )

        assertFalse(quality.isDistanceCorrect, "Lesion too far should fail distance check")
        assertFalse(quality.isOptimal)
    }

    @Test
    fun testBoundaryConditionsLighting() {
        // Test exact boundaries
        val atMin = useCase.execute(0.15f, 0.75f, 0.35f)
        assertTrue(atMin.isLightingGood, "Should pass at LIGHTING_MIN boundary")

        val belowMin = useCase.execute(0.14f, 0.75f, 0.35f)
        assertFalse(belowMin.isLightingGood, "Should fail just below LIGHTING_MIN")

        val atMax = useCase.execute(0.95f, 0.75f, 0.35f)
        assertTrue(atMax.isLightingGood, "Should pass at LIGHTING_MAX boundary")

        val aboveMax = useCase.execute(0.96f, 0.75f, 0.35f)
        assertFalse(aboveMax.isLightingGood, "Should fail just above LIGHTING_MAX")
    }
}