package com.app.modularadas.domain.repository

import android.graphics.Bitmap
import android.graphics.RectF

/**
 * A pure data class representing the raw output from ML model, 
 * before any monocular geometry is applied.
 */
data class RawDetection(
    val boundingBox: RectF, // Coordinates relative to the input frame
    val label: String,
    val confidence: Float
)


interface ObjectDetector {
    /**
     * Analyzes a single frame and returns a list of detected objects.
     */
    suspend fun detect(frame: Bitmap): List<RawDetection>
}