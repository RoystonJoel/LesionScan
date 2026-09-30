package org.lesionscan.project.infrastructure.test

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

object ImageTestUtils {

    /**
     * Load image from assets and convert to normalized float array [0, 1].
     * Uses inSampleSize downsampling and bulk pixel extraction to minimize heap allocations.
     */
    fun loadImageAsFloatArray(
        context: Context,
        assetPath: String,
        targetSize: Int = 224
    ): FloatArray {
        // 1. Decode bounds to compute optimal inSampleSize and prevent large bitmap allocations
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.assets.open(assetPath).use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }

        var inSampleSize = 1
        val rawWidth = boundsOptions.outWidth
        val rawHeight = boundsOptions.outHeight
        if (rawHeight > targetSize || rawWidth > targetSize) {
            val halfHeight = rawHeight / 2
            val halfWidth = rawWidth / 2
            while ((halfHeight / inSampleSize) >= targetSize && (halfWidth / inSampleSize) >= targetSize) {
                inSampleSize *= 2
            }
        }

        // 2. Decode sampled bitmap
        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val sampledBitmap = context.assets.open(assetPath).use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: throw RuntimeException("Failed to decode image: $assetPath")

        // 3. Resize to exact target size (224x224) if not already exact
        val resized = if (sampledBitmap.width == targetSize && sampledBitmap.height == targetSize) {
            sampledBitmap
        } else {
            val scaled = Bitmap.createScaledBitmap(sampledBitmap, targetSize, targetSize, true)
            sampledBitmap.recycle()
            scaled
        }

        // 4. Extract RGB pixels efficiently in bulk
        val pixels = IntArray(targetSize * targetSize)
        resized.getPixels(pixels, 0, targetSize, 0, 0, targetSize, targetSize)
        resized.recycle()

        // 5. Convert to normalized RGB [0, 1] float array
        val floatArray = FloatArray(targetSize * targetSize * 3)
        var idx = 0
        for (pixel in pixels) {
            floatArray[idx++] = ((pixel shr 16) and 0xFF) / 255f
            floatArray[idx++] = ((pixel shr 8) and 0xFF) / 255f
            floatArray[idx++] = (pixel and 0xFF) / 255f
        }

        return floatArray
    }

    /**
     * Retrieve list of image file names from an assets directory without loading them into memory.
     */
    fun getImageFiles(
        context: Context,
        directoryPath: String,
        limit: Int? = null
    ): List<String> {
        val files = context.assets.list(directoryPath)
            ?.filter { it.endsWith(".jpg") || it.endsWith(".png") }
            ?: emptyList()

        return if (limit != null) files.take(limit) else files
    }

    /**
     * Lazily load images one-by-one as a Sequence, allowing garbage collection
     * to free each image array before the next is loaded.
     */
    fun sequenceImages(
        context: Context,
        directoryPath: String,
        limit: Int? = null
    ): Sequence<Pair<String, FloatArray>> = sequence {
        val files = getImageFiles(context, directoryPath, limit)
        for (file in files) {
            val imageArray = loadImageAsFloatArray(context, "$directoryPath/$file")
            yield(file to imageArray)
        }
    }

    /**
     * Load images from assets directory into memory up to [limit] items (defaults to 30)
     * to prevent OutOfMemoryError.
     */
    fun loadImagesFromDirectory(
        context: Context,
        directoryPath: String,
        limit: Int = 30
    ): List<Pair<String, FloatArray>> {
        val files = getImageFiles(context, directoryPath, limit)
        return files.map { file ->
            file to loadImageAsFloatArray(context, "$directoryPath/$file")
        }
    }
}
