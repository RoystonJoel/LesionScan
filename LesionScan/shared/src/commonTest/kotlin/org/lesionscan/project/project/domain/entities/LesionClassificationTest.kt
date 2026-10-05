package org.lesionscan.project.domain.entities

import kotlin.test.Test
import kotlin.test.assertEquals

class LesionClassificationTest {

    @Test
    fun testRiskLevelMapping() {
        // Low risk
        val lowRisk = 0.2f.toRiskLevel()
        assertEquals(RiskLevel.LOW, lowRisk, "Score 0.2 should be LOW risk")

        // Medium risk
        val mediumRisk = 0.5f.toRiskLevel()
        assertEquals(RiskLevel.MEDIUM, mediumRisk, "Score 0.5 should be MEDIUM risk")

        // High risk
        val highRisk = 0.8f.toRiskLevel()
        assertEquals(RiskLevel.HIGH, highRisk, "Score 0.8 should be HIGH risk")
    }

    @Test
    fun testBoundaryRiskLevels() {
        // Boundary between LOW and MEDIUM (0.33)
        val atLowMediumBoundary = 0.33f.toRiskLevel()
        assertEquals(RiskLevel.MEDIUM, atLowMediumBoundary, "Score 0.33 should be MEDIUM")

        val justBelow = 0.32f.toRiskLevel()
        assertEquals(RiskLevel.LOW, justBelow, "Score 0.32 should be LOW")

        // Boundary between MEDIUM and HIGH (0.67)
        val atMediumHighBoundary = 0.67f.toRiskLevel()
        assertEquals(RiskLevel.HIGH, atMediumHighBoundary, "Score 0.67 should be HIGH")

        val justBelow67 = 0.66f.toRiskLevel()
        assertEquals(RiskLevel.MEDIUM, justBelow67, "Score 0.66 should be MEDIUM")
    }

    @Test
    fun testClassificationResult() {
        val classification = LesionClassification(
            riskScore = 0.75f,
            riskLevel = RiskLevel.HIGH,
            confidenceScore = 0.92f,
            inferenceTimeMs = 245
        )

        assertEquals(0.75f, classification.riskScore)
        assertEquals(RiskLevel.HIGH, classification.riskLevel)
        assertEquals(0.92f, classification.confidenceScore)
        assertEquals(245L, classification.inferenceTimeMs)
    }
}