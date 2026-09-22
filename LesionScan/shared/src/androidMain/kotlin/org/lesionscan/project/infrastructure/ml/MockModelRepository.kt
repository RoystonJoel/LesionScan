package org.lesionscan.project.infrastructure.ml


import kotlinx.coroutines.delay
import org.lesionscan.project.domain.usecases.IModelRepository
import kotlin.random.Random

/**
 * Mock implementation of IModelRepository for testing.
 * Simulates inference without loading a real TFLite model.
 *
 * Use this during development until real model is available.
 */
class MockModelRepository : IModelRepository {

    override suspend fun inferenceModel(frameImageArray: FloatArray): Float {
        // Simulate inference latency (real model takes 200-500ms on mid-range hardware)
        delay(Random.nextLong(200, 500))

        // Return a random risk score for testing
        return Random.nextFloat()
    }
}