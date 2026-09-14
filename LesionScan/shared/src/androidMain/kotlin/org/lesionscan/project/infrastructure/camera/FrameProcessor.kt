package org.lesionscan.project.infrastructure.camera

/**
 * Processes camera frames to extract quality metrics.
 *
 * Computes:
 * - Lighting level (brightness from histogram)
 * - Sharpness score (Laplacian edge detection)
 * - Lesion area percentage (basic blob detection)
 *
 * These metrics feed into ValidateFrameUseCase.
 */
class FrameProcessor {

    /**
     * Calculate lighting level from pixel data.
     * Returns the average brightness directly (simpler, more accurate).
     * @param imageArray Normalized float array [0, 1]
     * @return Lighting level [0, 1] where 1 = fully bright
     */
    fun calculateLighting(imageArray: FloatArray): Float {
        if (imageArray.isEmpty()) return 0f

        // Simply return average brightness
        return imageArray.average().toFloat().coerceIn(0f, 1f)
    }

    /**
     * Calculate sharpness using Laplacian variance.
     * Higher variance = sharper image.
     * @param imageArray Normalized float array
     * @param width Image width
     * @param height Image height
     * @return Sharpness score [0, 1]
     */
    fun calculateSharpness(imageArray: FloatArray, width: Int, height: Int): Float {
        if (imageArray.isEmpty() || width < 3 || height < 3) return 0f

        // Apply Laplacian kernel to detect edges
        var laplacianSum = 0f
        val kernel = intArrayOf(-1, -1, -1, -1, 8, -1, -1, -1, -1)

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = (y * width + x) * 3  // Assuming 3 channels
                val pixel = imageArray[idx]

                // Simple Laplacian approximation
                val neighbors = floatArrayOf(
                    imageArray[(y - 1) * width + (x - 1)],
                    imageArray[(y - 1) * width + x],
                    imageArray[(y - 1) * width + (x + 1)],
                    imageArray[y * width + (x - 1)],
                    pixel,
                    imageArray[y * width + (x + 1)],
                    imageArray[(y + 1) * width + (x - 1)],
                    imageArray[(y + 1) * width + x],
                    imageArray[(y + 1) * width + (x + 1)]
                )

                laplacianSum += Math.abs(kernel[4] * pixel + neighbors.drop(1).sum()).toFloat()
            }
        }

        // Normalize to [0, 1]
        // Higher variance = higher score
        val avgLaplacian = laplacianSum / ((width - 2) * (height - 2))
        return Math.min(avgLaplacian / 0.1f, 1f).toFloat()
    }

    /**
     * Estimate lesion area as percentage of frame.
     * Uses simple blob detection: find dark pixels likely to be lesion.
     * @param imageArray Normalized float array
     * @return Lesion area as percentage [0, 1]
     */
    fun calculateLesionArea(imageArray: FloatArray): Float {
        if (imageArray.isEmpty()) return 0f

        // Count pixels darker than threshold (lesions are typically darker than surrounding skin)
        val darkThreshold = 0.4f
        val darkPixelCount = imageArray.count { it < darkThreshold }
        val lesionArea = darkPixelCount.toFloat() / imageArray.size

        return lesionArea.coerceIn(0f, 1f)
    }
}