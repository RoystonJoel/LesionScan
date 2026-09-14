package org.lesionscan.project.infrastructure.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.concurrent.Executors

/**
 * Manages camera access and frame streaming via CameraX.
 *
 * Responsibilities:
 * - Bind camera to lifecycle
 * - Stream raw camera frames to analysis
 * - Convert frames to usable format (FloatArray for ML)
 * - Handle camera permissions & errors
 */
class CameraStreamingEngine(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {

    private var cameraProvider: ProcessCameraProvider? = null
    private val frameChannel = Channel<FrameData>(capacity = 10)

    data class FrameData(
        val imageArray: FloatArray,  // Normalized pixel data [0, 1]
        val width: Int,
        val height: Int,
        val timestamp: Long
    )

    /**
     * Get frame stream as Flow.
     * Frames flow from camera → normalized pixel data.
     */
    fun getFrameStream(): Flow<FrameData> = frameChannel.receiveAsFlow()

    /**
     * Start camera and begin streaming frames.
     */
    suspend fun startCamera() {
        try {
            cameraProvider = ProcessCameraProvider.getInstance(context).let { future ->
                future.get()  // Block until ready
            }

            val cameraProvider = cameraProvider ?: throw RuntimeException("Camera provider not available")

            // Unbind any previous use cases
            cameraProvider.unbindAll()

            // Create image analysis use case
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            imageAnalysis.setAnalyzer(
                Executors.newSingleThreadExecutor()
            ) { imageProxy ->
                processFrame(imageProxy)
            }

            // Select rear camera
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            // Bind to lifecycle
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                imageAnalysis
            )

            println("✓ Camera started")

        } catch (e: Exception) {
            println("✗ Camera start failed: ${e.message}")
            throw RuntimeException("Failed to start camera", e)
        }
    }

    /**
     * Stop camera and close resources.
     */
    fun stopCamera() {
        cameraProvider?.unbindAll()
        frameChannel.close()
        println("✓ Camera stopped")
    }

    /**
     * Process incoming frame from CameraX.
     * Convert to normalized float array for ML.
     */
    private fun processFrame(imageProxy: ImageProxy) {
        try {
            val width = imageProxy.width
            val height = imageProxy.height

            // Convert YUV420 → RGBA → Float array
            val planes = imageProxy.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride

            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            // Simple grayscale conversion for now
            // TODO: Replace with proper color space conversion for RGB model input
            val floatArray = FloatArray(width * height * 3)
            for (i in data.indices) {
                val normalized = (data[i].toInt() and 0xFF) / 255f
                floatArray[i * 3] = normalized
                floatArray[i * 3 + 1] = normalized
                floatArray[i * 3 + 2] = normalized
            }

            // Send frame to channel (non-blocking, drops oldest if buffer full)
            frameChannel.trySend(
                FrameData(
                    imageArray = floatArray,
                    width = width,
                    height = height,
                    timestamp = System.currentTimeMillis()
                )
            )

        } catch (e: Exception) {
            println("✗ Frame processing failed: ${e.message}")
        } finally {
            imageProxy.close()
        }
    }
}