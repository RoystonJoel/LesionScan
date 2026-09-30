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
    fun testMelanomaImagesClassifiedAsHighRisk() {
        runBlocking {
            // Test a representative sample of 30 images to prevent OOM
            val sampleSize = 30
            val melanomaFiles = ImageTestUtils.getImageFiles(context, "melanoma", limit = sampleSize)

            println("Testing ${melanomaFiles.size} melanoma images...")

            var highRiskCount = 0
            for (filename in melanomaFiles) {
                // Load one image at a time so memory can be reclaimed after each inference
                val imageArray = ImageTestUtils.loadImageAsFloatArray(context, "melanoma/$filename")
                val classification = classifyUseCase.execute(imageArray)

                println("Melanoma [$filename]: risk=${String.format("%.2f", classification.riskScore)}, level=${classification.riskLevel}")

                // Melanoma images should typically get MEDIUM or HIGH risk
                assert(
                    classification.riskLevel in listOf(RiskLevel.MEDIUM, RiskLevel.HIGH),
                    { "Melanoma image '$filename' should be MEDIUM or HIGH risk, got ${classification.riskLevel}" }
                )

                if (classification.riskLevel == RiskLevel.HIGH) {
                    highRiskCount++
                }
            }

            // At least 50% should be HIGH risk for good model
            val threshold = melanomaFiles.size / 2
            assert(highRiskCount >= threshold, { "At least 50% of melanoma images should be HIGH risk, got $highRiskCount/${melanomaFiles.size}" })
        }
    }

    @Test
    fun testNonMelanomaImagesClassifiedAsLowRisk() {
        runBlocking {
            // Test a representative sample of 30 images to prevent OOM
            val sampleSize = 30
            val benignFiles = ImageTestUtils.getImageFiles(context, "non_melanoma", limit = sampleSize)

            println("Testing ${benignFiles.size} non-melanoma images...")

            var lowRiskCount = 0
            for (filename in benignFiles) {
                // Load one image at a time so memory can be reclaimed after each inference
                val imageArray = ImageTestUtils.loadImageAsFloatArray(context, "non_melanoma/$filename")
                val classification = classifyUseCase.execute(imageArray)

                println("Non-Melanoma [$filename]: risk=${String.format("%.2f", classification.riskScore)}, level=${classification.riskLevel}")

                // Benign images should typically get LOW or MEDIUM risk
                assert(
                    classification.riskLevel in listOf(RiskLevel.LOW, RiskLevel.MEDIUM),
                    { "Benign image '$filename' should be LOW or MEDIUM risk, got ${classification.riskLevel}" }
                )

                if (classification.riskLevel == RiskLevel.LOW) {
                    lowRiskCount++
                }
            }

            // At least 50% should be LOW risk for good model
            val threshold = benignFiles.size / 2
            assert(lowRiskCount >= threshold, { "At least 50% of benign images should be LOW risk, got $lowRiskCount/${benignFiles.size}" })
        }
    }

    @Test
    fun testModelInferenceTime() {
        runBlocking {
            val melanomaFiles = ImageTestUtils.getImageFiles(context, "melanoma", limit = 1)

            if (melanomaFiles.isEmpty()) {
                println("No test images found, skipping inference time test")
                return@runBlocking
            }

            val filename = melanomaFiles.first()
            val imageArray = ImageTestUtils.loadImageAsFloatArray(context, "melanoma/$filename")
            val classification = classifyUseCase.execute(imageArray)

            println("Inference time: ${classification.inferenceTimeMs}ms")

            // Should be sub-1 second per Milestone 1 spec
            assert(
                classification.inferenceTimeMs < 1000,
                { "Inference should be < 1000ms, got ${classification.inferenceTimeMs}ms" }
            )

            // Should be reasonably fast (< 500ms on mid-range hardware)
            if (classification.inferenceTimeMs > 500) {
                println("⚠ Warning: Inference took ${classification.inferenceTimeMs}ms (target < 500ms)")
            }
        }
    }

    @Test
    fun testModelConfidenceScores() {
        runBlocking {
            val melanomaFiles = ImageTestUtils.getImageFiles(context, "melanoma", limit = 20)

            for (filename in melanomaFiles) {
                val imageArray = ImageTestUtils.loadImageAsFloatArray(context, "melanoma/$filename")
                val classification = classifyUseCase.execute(imageArray)

                // Confidence should be reasonable (> 0.6)
                assert(
                    classification.confidenceScore > 0.6f,
                    { "Confidence should be > 0.6, got ${classification.confidenceScore}" }
                )

                println("$filename: confidence=${String.format("%.2f", classification.confidenceScore)}")
            }
        }
    }
}
