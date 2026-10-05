package org.lesionscan.project.infrastructure.ml

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.lesionscan.project.domain.usecases.IModelRepository
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ModelSandboxManager(
    private val context: Context
) : IModelRepository {

    private var interpreter: Interpreter? = null
    private val modelName = "lesion_classifier_tflite.tflite"
    private val initializationMutex = Mutex()

    // Pre-allocate buffer once: 1 batch * 224 width * 224 height * 3 channels * 4 bytes per float
    private val inputBuffer = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4).apply {
        order(ByteOrder.nativeOrder())
    }

    // Pre-allocate output array
    private val outputData = Array(1) { FloatArray(5) }

    private suspend fun initializeModel() {
        if (interpreter != null) return

        initializationMutex.withLock {
            if (interpreter != null) return@withLock
            try {
                val modelBuffer = loadModelFromAssets(modelName)
                interpreter = Interpreter(modelBuffer)
                println("✓ Model loaded successfully")
            } catch (e: Exception) {
                println("✗ Failed to load model: ${e.message}")
                throw RuntimeException("ModelSandboxManager initialization failed", e)
            }
        }
    }

    private fun loadModelFromAssets(modelPath: String): MappedByteBuffer {
        context.assets.openFd(modelPath).use { assetFileDescriptor ->
            FileInputStream(assetFileDescriptor.fileDescriptor).use { fileInputStream ->
                val fileChannel = fileInputStream.channel
                return fileChannel.map(
                    FileChannel.MapMode.READ_ONLY,
                    assetFileDescriptor.startOffset,
                    assetFileDescriptor.declaredLength
                )
            }
        }
    }

    override suspend fun inferenceModel(frameImageArray: FloatArray): Float {
        return withContext(Dispatchers.Default) {
            initializeModel()

            val activeInterpreter = interpreter
                ?: throw RuntimeException("Interpreter not initialized")

            try {
                // 1. Reset buffer position and load flat array directly
                inputBuffer.rewind()

                val expectedSize = 224 * 224 * 3
                val limit = minOf(frameImageArray.size, expectedSize)

                // Safely copy only up to the buffer's capacity
                for (i in 0 until limit) {
                    inputBuffer.putFloat(frameImageArray[i])
                }

                // Pad with zeros if the array is smaller than expected
                for (i in limit until expectedSize) {
                    inputBuffer.putFloat(0f)
                }

                // 2. Run inference using the pre-allocated buffer and array
                val startTime = System.currentTimeMillis()
                activeInterpreter.run(inputBuffer, outputData)
                val inferenceTimeMs = System.currentTimeMillis() - startTime

                // 3. Extract and evaluate results
                val probabilities = outputData[0]
                println("Probabilities: ${probabilities.contentToString()}")

                // Find class with highest probability
                var predictedClassIndex = 0
                var maxProbability = 0f
                for (i in probabilities.indices) {
                    if (probabilities[i] > maxProbability) {
                        maxProbability = probabilities[i]
                        predictedClassIndex = i
                    }
                }

                println("✓ Predicted class: $predictedClassIndex with probability: ${String.format("%.2f", maxProbability)}")
                println("✓ Inference completed in ${inferenceTimeMs}ms")

                // Map to risk score
                val riskScore = when (predictedClassIndex) {
                    0 -> 0.9f   // Class 0 (likely Melanoma or High risk)
                    1 -> 0.7f   // Class 1 (Medium risk)
                    2 -> 0.5f   // Class 2 (Medium-low risk)
                    3 -> 0.2f   // Class 3 (Low risk)
                    4 -> 0.2f   // Class 4 (Benign/Low risk)
                    else -> 0.2f
                } * maxProbability

                riskScore.coerceIn(0f, 1f)

            } catch (e: Exception) {
                println("✗ Inference failed: ${e.message}")
                e.printStackTrace()
                throw RuntimeException("Inference execution failed", e)
            }
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}