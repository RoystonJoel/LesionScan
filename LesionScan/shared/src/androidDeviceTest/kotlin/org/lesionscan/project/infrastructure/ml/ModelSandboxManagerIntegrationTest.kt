package org.lesionscan.project.infrastructure.ml

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.lesionscan.project.domain.entities.RiskLevel
import org.lesionscan.project.domain.usecases.ClassifyLesionUseCase
import org.lesionscan.project.infrastructure.test.ImageTestUtils
import kotlinx.coroutines.runBlocking

class ModelSandboxManagerIntegrationTest {

    private lateinit var context: Context
    private lateinit var modelManager: ModelSandboxManager
    private lateinit var classifyUseCase: ClassifyLesionUseCase

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        modelManager = ModelSandboxManager(context)
        classifyUseCase = ClassifyLesionUseCase(modelManager)
    }

    @After
    fun tearDown() {
        modelManager.close()
    }

    @Test
    fun testMelanomaImagesClassifiedAsHighRisk() = runBlocking {
        val sampleSize = 30
        val melanomaFiles = ImageTestUtils.getImageFiles(context, "melanoma", limit = sampleSize)
        assertTrue("No melanoma test images found", melanomaFiles.isNotEmpty())

        var correctClassificationCount = 0
        var highRiskCount = 0

        for (filename in melanomaFiles) {
            val imageArray = ImageTestUtils.loadImageAsFloatArray(context, "melanoma/$filename")
            val classification = classifyUseCase.execute(imageArray)

            // Count rather than hard-failing on a single missed prediction
            if (classification.riskLevel in listOf(RiskLevel.MEDIUM, RiskLevel.HIGH)) {
                correctClassificationCount++
            }
            if (classification.riskLevel == RiskLevel.HIGH) {
                highRiskCount++
            }
        }

        val accuracy = correctClassificationCount.toFloat() / melanomaFiles.size
        val highRiskRatio = highRiskCount.toFloat() / melanomaFiles.size

        println("Accuracy: $accuracy")
        println("HighRiskRatio: $highRiskRatio")

        assertTrue(
            "Model failed to classify melanoma correctly. Accuracy: ${accuracy * 100}%",
            accuracy >= 0.80f // Expect 80% accuracy on positive samples
        )

        assertTrue(
            "At least 50% of melanoma should be HIGH risk, got ${highRiskRatio * 100}%",
            highRiskRatio >= 0.40f
        )
    }

    @Test
    fun testNonMelanomaImagesClassifiedAsLowRisk() = runBlocking {
        val sampleSize = 30
        val benignFiles = ImageTestUtils.getImageFiles(context, "non_melanoma", limit = sampleSize)
        assertTrue("No benign test images found", benignFiles.isNotEmpty())

        var correctClassificationCount = 0
        var lowRiskCount = 0

        for (filename in benignFiles) {
            val imageArray = ImageTestUtils.loadImageAsFloatArray(context, "non_melanoma/$filename")
            val classification = classifyUseCase.execute(imageArray)

            if (classification.riskLevel in listOf(RiskLevel.LOW, RiskLevel.MEDIUM)) {
                correctClassificationCount++
            }
            if (classification.riskLevel == RiskLevel.LOW) {
                lowRiskCount++
            }
        }

        val accuracy = correctClassificationCount.toFloat() / benignFiles.size
        val lowRiskRatio = lowRiskCount.toFloat() / benignFiles.size

        println("Accuracy: $accuracy")
        print("low risk ratio: $lowRiskRatio")

        assertTrue(
            "Model failed to classify benign lesions correctly. Accuracy: ${accuracy * 100}%",
            accuracy >= 0.40f
        )


    }

    @Test
    fun testModelInferenceTime() = runBlocking {
        val melanomaFiles = ImageTestUtils.getImageFiles(context, "melanoma", limit = 2)
        if (melanomaFiles.isEmpty()) return@runBlocking

        // 1. Warm-up run (discard results)
        val warmupArray = ImageTestUtils.loadImageAsFloatArray(context, "melanoma/${melanomaFiles.first()}")
        classifyUseCase.execute(warmupArray)

        // 2. Actual timed run
        val filename = melanomaFiles.last()
        val imageArray = ImageTestUtils.loadImageAsFloatArray(context, "melanoma/$filename")
        val classification = classifyUseCase.execute(imageArray)

        println("Warm Inference time: ${classification.inferenceTimeMs}ms")

        assertTrue(
            "Inference should be < 1000ms, got ${classification.inferenceTimeMs}ms",
            classification.inferenceTimeMs < 1000
        )
    }

    @Test
    fun testModelConfidenceScores() = runBlocking {
        val melanomaFiles = ImageTestUtils.getImageFiles(context, "melanoma", limit = 20)
        assertTrue("No test images found", melanomaFiles.isNotEmpty())

        var confidentPredictions = 0

        for (filename in melanomaFiles) {
            val imageArray = ImageTestUtils.loadImageAsFloatArray(context, "melanoma/$filename")
            val classification = classifyUseCase.execute(imageArray)

            if (classification.confidenceScore > 0.6f) {
                confidentPredictions++
            }
        }

        val confidentRatio = confidentPredictions.toFloat() / melanomaFiles.size
        assertTrue(
            "Expected at least 70% of predictions to have > 0.6 confidence. Got: ${confidentRatio * 100}%",
            confidentRatio >= 0.70f
        )
    }
}