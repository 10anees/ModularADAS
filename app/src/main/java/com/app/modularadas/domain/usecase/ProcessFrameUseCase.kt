package com.app.modularadas.domain.usecase

import android.graphics.Bitmap
import android.util.Log
import com.app.modularadas.core.math.DistanceEstimator
import com.app.modularadas.domain.models.CameraExtrinsics
import com.app.modularadas.domain.models.DetectionResult
import com.app.modularadas.domain.repository.ObjectDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Orchestrates TFLite inference and distance calculation.
 */
class ProcessFrameUseCase(
    private val objectDetector: ObjectDetector
) {
    companion object {
        private const val TAG = "ProcessFrameUseCase"
    }

    /**
     * Processes a single frame through the computer vision pipeline.
     * Runs entirely on the Default dispatcher to prevent blocking the Main (UI) thread.
     *
     * @param frame The current camera frame.
     * @param extrinsics The camera's mounting height and pitch angle.
     * @param verticalFovDegrees The dynamically extracted field of view.
     * @return A list of completely processed DetectionResults, including physical distance.
     */
    suspend operator fun invoke(
        frame: Bitmap,
        extrinsics: CameraExtrinsics,
        verticalFovDegrees: Float
    ): List<DetectionResult> = withContext(Dispatchers.Default) {
        Log.d(TAG, "[Pipeline] Starting frame processing: ${frame.width}x${frame.height}, FOV=$verticalFovDegrees°, height=${extrinsics.heightMeters}m, pitch=${extrinsics.pitchAngleDegrees}°")
        
        // 1. Run inference via the injected ML Engine
        val rawDetections = objectDetector.detect(frame)
        Log.d(TAG, "[Pipeline] Raw detections received: count=${rawDetections.size}")
        
        if (rawDetections.isEmpty()) {
            Log.d(TAG, "[Pipeline] No detections to process, returning empty list")
            return@withContext emptyList()
        }

        // 2. Map 2D pixel coordinates to 3D physical distances
        val processedDetections = rawDetections.map { raw ->
            val distance = DistanceEstimator.estimateDistance(
                cameraHeightMeters = extrinsics.heightMeters,
                cameraPitchDegrees = extrinsics.pitchAngleDegrees,
                verticalFovDegrees = verticalFovDegrees,
                frameHeightPixels = frame.height,
                boxBottomY = raw.boundingBox.bottom
            )
            Log.d(TAG, "[Pipeline] Distance calculation: label='${raw.label}' confidence=${raw.confidence} estimated_distance=${distance}m")

            DetectionResult(
                boundingBox = raw.boundingBox,
                label = raw.label,
                confidence = raw.confidence,
                estimatedDistanceMeters = distance
            )
        }
        
        // 3. Filter out mathematical anomalies
        val filtered = processedDetections.filter { 
            // Sanity check: Filter out mathematical anomalies (e.g., bounding boxes above the horizon)
            it.estimatedDistanceMeters > 0f && it.estimatedDistanceMeters < 150f 
        }
        
        Log.d(TAG, "[Pipeline] Filtering complete: before=${processedDetections.size}, after=${filtered.size}")
        if (processedDetections.size != filtered.size) {
            Log.d(TAG, "[Pipeline] Filtered out ${processedDetections.size - filtered.size} anomalies")
            processedDetections.forEach { det ->
                if (!filtered.contains(det)) {
                    Log.d(TAG, "[Pipeline] Anomaly filtered: distance=${det.estimatedDistanceMeters}m (outside 0-150m range)")
                }
            }
        }
        
        filtered
    }
}