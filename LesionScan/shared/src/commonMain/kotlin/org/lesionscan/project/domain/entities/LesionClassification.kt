package org.lesionscan.project.domain.entities


/**
 * Output of the ML inference engine.
 * Risk classification from the TensorFlow Lite model.
 */
data class LesionClassification(
    val riskScore: Float,              // 0.0 - 1.0 (higher = higher risk)
    val riskLevel: RiskLevel,          // LOW, MEDIUM, HIGH
    val confidenceScore: Float,        // Model confidence 0.0 - 1.0
    val inferenceTimeMs: Long          // How long inference took
)

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

fun Float.toRiskLevel(): RiskLevel = when {
    this < 0.33f -> RiskLevel.LOW
    this < 0.67f -> RiskLevel.MEDIUM
    else -> RiskLevel.HIGH
}