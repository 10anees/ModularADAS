package com.app.modularadas.domain.usecase

import com.app.modularadas.core.constants.AdasConfig
import com.app.modularadas.domain.models.AlertLevel
import com.app.modularadas.domain.models.DetectionResult

/**
 * Maps calculated distance to specific AlertLevels
 */
class EvaluateAlertUseCase {

    /**
     * Analyzes the current frame's detections and determines the highest active threat.
     */
    operator fun invoke(detections: List<DetectionResult>): AlertLevel {
        if (detections.isEmpty()) return AlertLevel.SAFE

        // Isolate the obstacle closest to the camera
        val nearestObstacle = detections.minByOrNull { it.estimatedDistanceMeters } 
            ?: return AlertLevel.SAFE

        val distance = nearestObstacle.estimatedDistanceMeters

        // Prioritize the most severe alerts first
        return when {
            distance <= AdasConfig.THRESHOLD_CRITICAL_METERS -> AlertLevel.CRITICAL
            distance <= AdasConfig.THRESHOLD_WARNING_METERS -> AlertLevel.WARNING
            else -> AlertLevel.SAFE
        }
    }
}