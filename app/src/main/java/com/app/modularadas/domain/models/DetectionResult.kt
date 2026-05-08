package com.app.modularadas.domain.models

import android.graphics.RectF

/**
 * Represents a single detected object in the frame.
 */
data class DetectionResult(
    val boundingBox: RectF, // Normalized coordinates (0.0 to 1.0)
    val label: String,      // e.g., "car", "person"
    val confidence: Float,
    val estimatedDistanceMeters: Float
)