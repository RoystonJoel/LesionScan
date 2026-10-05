package org.lesionscan.project.infrastructure.camera


import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FrameProcessorTest {

    private val processor = FrameProcessor()

    @Test
    fun testLightingCalculation() {
        // Dark image (avg brightness 0.2)
        val darkPixels = FloatArray(1000) { 0.2f }
        val darkLighting = processor.calculateLighting(darkPixels)
        assertTrue(darkLighting < 0.3f, "Dark image should have low lighting score")

        // Bright image (avg brightness 0.5)
        val brightPixels = FloatArray(1000) { 0.5f }
        val brightLighting = processor.calculateLighting(brightPixels)
        assertTrue(brightLighting in 0.4f..0.6f, "Mid-bright image should have moderate lighting")

        // Overexposed (avg brightness 0.95)
        val overexposedPixels = FloatArray(1000) { 0.95f }
        val overexposedLighting = processor.calculateLighting(overexposedPixels)
        assertTrue(overexposedLighting > 0.8f, "Overexposed image should have high lighting score")
    }

    @Test
    fun testSharpnessCalculation() {
        // Uniform (blurry) image
        val blurryPixels = FloatArray(400) { 0.5f }  // 20x20 uniform gray
        val blurryScore = processor.calculateSharpness(blurryPixels, 20, 20)
        assertTrue(blurryScore < 0.3f, "Uniform image should have low sharpness")

        // High contrast (sharp) image
        val sharpPixels = FloatArray(400) { i ->
            if (i % 2 == 0) 0.2f else 0.8f  // Alternating dark/bright = lots of edges
        }
        val sharpScore = processor.calculateSharpness(sharpPixels, 20, 20)
        assertTrue(sharpScore > 0.4f, "High contrast image should have higher sharpness")
    }

    @Test
    fun testLesionAreaCalculation() {
        // Image with 20% dark pixels (lesion)
        val lesionPixels = FloatArray(1000) { i ->
            if (i < 200) 0.2f else 0.8f  // First 200 pixels are dark (lesion)
        }
        val lesionArea = processor.calculateLesionArea(lesionPixels)
        assertTrue(lesionArea in 0.15f..0.25f, "Should detect ~20% lesion area")

        // Image with mostly dark pixels
        val dominantLesionPixels = FloatArray(1000) { 0.3f }
        val dominantArea = processor.calculateLesionArea(dominantLesionPixels)
        assertTrue(dominantArea > 0.5f, "Should detect high lesion area")
    }
}