package org.lesionscan.project.presentation.viewmodels

import org.lesionscan.project.presentation.FrameQualityState
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FrameQualityStateTest {

    @Test
    fun testAllGreenIsOptimal() {
        val state = FrameQualityState(
            isLighting = true,
            isFocus = true,
            isDistance = true,
            isOptimal = true
        )

        assertTrue(state.isOptimal, "All green should be optimal")
    }

    @Test
    fun testOneRedIsNotOptimal() {
        val state = FrameQualityState(
            isLighting = false,  // RED
            isFocus = true,
            isDistance = true,
            isOptimal = false
        )

        assertFalse(state.isOptimal, "Any red should not be optimal")
    }

    @Test
    fun testProgressBarValues() {
        val state = FrameQualityState(
            lightingLevel = 0.5f,
            focusLevel = 0.75f,
            distanceLevel = 0.35f
        )

        assertTrue(state.lightingLevel in 0f..1f)
        assertTrue(state.focusLevel in 0f..1f)
        assertTrue(state.distanceLevel in 0f..1f)
    }
}