package org.lesionscan.project.domain.usecases

import org.lesionscan.project.domain.entities.LesionClassification
import org.lesionscan.project.domain.entities.toRiskLevel

/**
 * Business logic for running ML inference on a frame.
 *
 * This use case coordinates with the ML infrastructure layer
 * (ModelSandboxManager) to execute the TensorFlow Lite model
 * and wrap the output in a domain entity.
 */
class ClassifyLesionUseCase(
    private val modelRepository: IModelRepository
) {

    /**
     * Run inference on a frame tensor.
     *
     * @param frameImageArray Pixel data (e.g., YUV420 or RGB)
     * @return Classification with risk score and timing
     */
    suspend fun execute(frameImageArray: FloatArray): LesionClassification {
        val startTime = System.currentTimeMillis()

        // Delegate to infrastructure layer to run the model
        val riskScore = modelRepository.inferenceModel(frameImageArray)

        val endTime = System.currentTimeMillis()
        val inferenceTimeMs = endTime - startTime

        return LesionClassification(
            riskScore = riskScore,
            riskLevel = riskScore.toRiskLevel(),
            confidenceScore = 0.95f,  // Placeholder; real model returns this
            inferenceTimeMs = inferenceTimeMs
        )
    }
}

/**
 * Repository interface for ML model access.
 * Implementation lives in infrastructure layer (Android-specific).
 */
interface IModelRepository {
    suspend fun inferenceModel(frameImageArray: FloatArray): Float
}