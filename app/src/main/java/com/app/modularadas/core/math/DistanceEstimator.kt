package com.app.modularadas.core.math

import kotlin.math.tan

// Handles monocular geometry calculations
object DistanceEstimator {

    /**
     * @param cameraHeightMeters Physical height of the device from the road.
     * @param cameraPitchDegrees Downward/Upward tilt of the phone.
     * @param verticalFovDegrees Device-specific vertical FOV.
     * @param frameHeightPixels Total height of the camera frame.
     * @param boxBottomY The Y coordinate of the bottom edge of the bounding box.
     * @return Estimated distance in meters.
     */
    fun estimateDistance(
        cameraHeightMeters: Float,
        cameraPitchDegrees: Float,
        verticalFovDegrees: Float,
        frameHeightPixels: Int,
        boxBottomY: Float
    ): Float {
        // Calculate the angle of the pixel relative to the optical center
        val centerPoint = frameHeightPixels / 2f
        val deltaY = boxBottomY - centerPoint
        
        // Degrees per pixel approximation
        val degreesPerPixel = verticalFovDegrees / frameHeightPixels
        val pixelAngle = deltaY * degreesPerPixel

        // Total angle to the ground contact point
        val totalAngle = Math.toRadians((cameraPitchDegrees + pixelAngle).toDouble())

        // Prevent division by zero or negative distances if bounding box is above horizon
        if (totalAngle <= 0.0) return Float.MAX_VALUE

        // Basic trigonometry: Distance = Height / tan(theta)
        return (cameraHeightMeters / tan(totalAngle)).toFloat()
    }
}