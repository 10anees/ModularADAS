package com.app.modularadas.core.constants

// Contains focal length approximations and alert distance thresholds.
object AdasConfig {
    // Alert Thresholds (Meters)
    const val THRESHOLD_WARNING_METERS = 7.0f 
    const val THRESHOLD_CRITICAL_METERS = 3.0f

    // Camera Defaults (Can be overridden by CalibrationScreen)
    const val DEFAULT_CAMERA_HEIGHT_METERS = 1.3f // Typical sedan trunk height
    const val DEFAULT_CAMERA_PITCH_DEGREES = 0.0f // Perpendicular to ground

    // Vision Pipeline Constants
    const val TARGET_FPS = 30 // Target frame rate for CameraX
    const val INPUT_WIDTH = 320 // YOLOv8n quantized input dims
    const val INPUT_HEIGHT = 320
}