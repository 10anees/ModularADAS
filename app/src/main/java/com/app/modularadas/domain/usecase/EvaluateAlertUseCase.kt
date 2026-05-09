package com.app.modularadas.domain.usecase

import com.app.modularadas.domain.models.AlertLevel
import com.app.modularadas.domain.models.DetectionResult

/**
 * Maps calculated distance to specific AlertLevels
 */
class EvaluateAlertUseCase {

    /**
     * Analyzes the current frame's detections and determines the highest active threat.
     * @param detections List of detected objects in the current frame
     * @param warningThresholdMeters Distance below which YELLOW/WARNING alert is triggered
     * @param criticalThresholdMeters Distance below which RED/CRITICAL alert is triggered
     */
    operator fun invoke(
        detections: List<DetectionResult>,
        warningThresholdMeters: Float = 8.0f,  // Default from AdasConfig
        criticalThresholdMeters: Float = 3.0f  // Default from AdasConfig
    ): AlertLevel {
        if (detections.isEmpty()) return AlertLevel.SAFE

        // Isolate the obstacle closest to the camera
        val nearestObstacle = detections.minByOrNull { it.estimatedDistanceMeters } 
            ?: return AlertLevel.SAFE

        val distance = nearestObstacle.estimatedDistanceMeters

        // Prioritize the most severe alerts first
        return when {
            distance <= criticalThresholdMeters -> AlertLevel.CRITICAL
            distance <= warningThresholdMeters -> AlertLevel.WARNING
            else -> AlertLevel.SAFE
        }
    }
}