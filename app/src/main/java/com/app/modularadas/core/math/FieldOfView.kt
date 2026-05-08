package com.app.modularadas.core.util

import android.hardware.camera2.CameraCharacteristics
import android.util.SizeF
import kotlin.math.atan
import kotlin.math.PI

// Extracts intrinsic camera parameters programmatically to ensure device agnosticism.

object CameraCalibrationUtils {

    /**
     * Calculates the Vertical Field of View (FOV) in degrees.
     * 
     * @param characteristics The CameraCharacteristics of the active rear lens.
     * @return The vertical FOV in degrees, or a fallback default if hardware reports are missing.
     */
    fun getVerticalFov(characteristics: CameraCharacteristics): Float {
        val physicalSize: SizeF? = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
        val focalLengths: FloatArray? = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)

        if (physicalSize != null && focalLengths != null && focalLengths.isNotEmpty()) {
            val focalLength = focalLengths[0] // Assume primary focal length
            val sensorHeight = physicalSize.height
            
            // FOV = 2 * arctan(sensorHeight / (2 * focalLength))
            val fovRadians = 2.0 * atan((sensorHeight / (2 * focalLength)).toDouble())
            return (fovRadians * (180.0 / PI)).toFloat()
        }

        // Fallback for non-compliant devices (approximate average smartphone V-FOV)
        return 45.0f 
    }
}