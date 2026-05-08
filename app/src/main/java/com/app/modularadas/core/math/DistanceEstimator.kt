package com.app.modularadas.core.math

import kotlin.math.atan
import kotlin.math.tan

// Handles monocular geometry calculations using a pinhole approximation
object DistanceEstimator {

    /**
     * Estimate distance using pinhole geometry.
     *
     * - Computes focal length in pixels from vertical FOV:
     *   focal_px = (frameHeightPx / 2) / tan(vfov_rad / 2)
     * - Angle to pixel = atan(deltaY / focal_px)
     * - Total angle = cameraPitch + angleToPixel (radians)
     * - distance = cameraHeight / tan(totalAngle)
     */
    fun estimateDistance(
        cameraHeightMeters: Float,
        cameraPitchDegrees: Float,
        verticalFovDegrees: Float,
        frameHeightPixels: Int,
        boxBottomY: Float,
        focalPxFromCalibration: Float = 0f
    ): Float {
        val centerY = frameHeightPixels / 2f
        val deltaY = boxBottomY - centerY

        // Prefer using a calibrated focal length in pixels when available
        val focalPx = if (focalPxFromCalibration > 0f) {
            focalPxFromCalibration.toDouble()
        } else {
            // Derive focal length in pixels from vertical FOV
            val vfovRad = Math.toRadians(verticalFovDegrees.toDouble())
            (frameHeightPixels / 2.0) / kotlin.math.tan(vfovRad / 2.0)
        }

        // Angle from optical center to pixel (radians)
        val angleToPixel = atan(deltaY.toDouble() / focalPx)

        // Total angle = camera pitch (convert to radians) + pixel angle
        val totalAngleRad = Math.toRadians(cameraPitchDegrees.toDouble()) + angleToPixel

        if (totalAngleRad <= 0.0) return Float.MAX_VALUE

        return (cameraHeightMeters / tan(totalAngleRad)).toFloat()
    }
}