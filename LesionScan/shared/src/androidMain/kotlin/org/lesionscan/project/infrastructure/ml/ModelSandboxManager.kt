package org.lesionscan.project.infrastructure.ml

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.lesionscan.project.domain.usecases.IModelRepository
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
//import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
//import org.tensorflow.lite.DataType


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

                // Output shape [1, 5] - MUST be 2D to match tensor output
                val outputData = Array(1) { FloatArray(5) }

                // Run inference
                val startTime = System.currentTimeMillis()
                interpreter.run(inputData, outputData)
                val inferenceTimeMs = System.currentTimeMillis() - startTime

                // Extract probabilities from first (and only) batch
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
                    0 -> 0.9f   // Melanoma
                    2 -> 0.7f   // BCC
                    4 -> 0.5f   // Dermatofibroma
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