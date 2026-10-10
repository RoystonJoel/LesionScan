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

            val interpreter = interpreter
                ?: throw RuntimeException("Interpreter not initialized")

            try {
                // Reshape input to [1, 224, 224, 3]
                val inputData = Array(1) { Array(224) { Array(224) { FloatArray(3) } } }

                var idx = 0
                for (h in 0 until 224) {
                    for (w in 0 until 224) {
                        for (c in 0 until 3) {
                            inputData[0][h][w][c] = if (idx < frameImageArray.size) frameImageArray[idx++] else 0f
                        }
                    }
                }

                // Output shape [1, 1] - Single probability score
                val outputData = Array(1) { FloatArray(1) }

                val startTime = System.currentTimeMillis()
                interpreter.run(inputData, outputData)
                val inferenceTimeMs = System.currentTimeMillis() - startTime

                // Extract probability
                val melanomaProbability = outputData[0][0]  // Single value

                println("Melanoma probability: ${String.format("%.2f", melanomaProbability)}")

                // Direct mapping: probability = risk score
                // 0.7 = 70% chance of melanoma = HIGHinferenceTimeMs risk
                // 0.3 = 30% chance of melanoma = LOW risk
                val riskScore = melanomaProbability.coerceIn(0f, 1f)

                println("✓ Inference completed in ${inferenceTimeMs}ms")
                println("✓ Risk score: ${String.format("%.2f", riskScore)}")

                riskScore

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