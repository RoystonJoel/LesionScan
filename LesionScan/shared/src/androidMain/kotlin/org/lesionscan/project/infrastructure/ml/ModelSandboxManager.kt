package org.lesionscan.project.infrastructure.ml

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.lesionscan.project.domain.usecases.IModelRepository
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ModelSandboxManager(
    private val context: Context
) : IModelRepository {

    private var interpreter: Interpreter? = null
    private val modelName = "lesion_classifier_tflite.tflite"

    private fun initializeModel() {
        if (interpreter != null) return

        try {
            val modelBuffer = loadModelFromAssets(modelName)
            interpreter = Interpreter(modelBuffer)
            println("✓ Model loaded successfully")
        } catch (e: Exception) {
            println("✗ Failed to load model: ${e.message}")
            throw RuntimeException("ModelSandboxManager initialization failed", e)
        }
    }

    private fun loadModelFromAssets(modelPath: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelPath)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel

        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength

        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            startOffset,
            declaredLength
        )
    }

    override suspend fun inferenceModel(frameImageArray: FloatArray): Float {
        return withContext(Dispatchers.Default) {
            initializeModel()

            val interpreter = interpreter
                ?: throw RuntimeException("Interpreter not initialized")

            try {
                // Create input array (batch_size=1, 224x224x3)
                val inputShape = intArrayOf(1, 224, 224, 3)
                val inputData = Array(1) { Array(224) { Array(224) { FloatArray(3) } } }

                // Fill inputData from frameImageArray
                var idx = 0
                for (h in 0 until 224) {
                    for (w in 0 until 224) {
                        for (c in 0 until 3) {
                            inputData[0][h][w][c] = if (idx < frameImageArray.size) frameImageArray[idx++] else 0f
                        }
                    }
                }

                // Output buffer for single risk score
                val outputData = FloatArray(1)

                // Run inference
                val startTime = System.currentTimeMillis()
                interpreter.run(inputData, outputData)
                val inferenceTimeMs = System.currentTimeMillis() - startTime

                val riskScore = outputData[0]

                println("✓ Inference completed in ${inferenceTimeMs}ms, risk: $riskScore")

                riskScore.coerceIn(0f, 1f)

            } catch (e: Exception) {
                println("✗ Inference failed: ${e.message}")
                throw RuntimeException("Inference execution failed", e)
            }
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}