package org.lesionscan.project.domain.usecases


import org.lesionscan.project.domain.entities.RiskLevel
import kotlin.test.Test
import kotlin.test.assertTrue

class ValidateFrameUseCaseTest {

    @Test
    fun testOptimalFrameConditions() {
        val useCase = ValidateFrameUseCase()

        val quality = useCase.execute(
            lightingLevel = 0.5f,      // Good lighting
            sharpnessScore = 0.75f,    // Sharp
            lesionAreaPercentage = 0.35f  // Good distance
        )

        assertTrue(quality.isOptimal, "Frame should be optimal")
    }

    @Test
    fun testDarkFrameRejectsCapture() {
        val useCase = ValidateFrameUseCase()

        val quality = useCase.execute(
            lightingLevel = 0.1f,      // Too dark
            sharpnessScore = 0.75f,
            lesionAreaPercentage = 0.35f
        )

        assertTrue(!quality.isLightingGood, "Dark frame should fail lighting check")
        assertTrue(!quality.isOptimal, "Frame should not be optimal")
    }
}