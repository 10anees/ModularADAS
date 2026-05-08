package com.app.modularadas.ui.state

import android.graphics.RectF
import com.app.modularadas.domain.models.AlertLevel
import com.app.modularadas.domain.models.CameraExtrinsics
import androidx.compose.runtime.Immutable

@Immutable
enum class DashboardTab {
    Screen,
    Calibration,
    Network
}

@Immutable
data class MetricVisibility(
    val speed: Boolean = true,
    val distance: Boolean = true,
    val latency: Boolean = true
)

@Immutable
data class CalibrationUiState(
    val cameraHeightMeters: Float = 1.5f,
    val cameraTiltDegrees: Float = 15f,
    val focalLengthMm: Float = 35f,
    val vehicleWidthMeters: Float = 1.9f,
    val referenceDistanceMeters: Float = 3f,
    val warningDistanceMeters: Float = 8f,
    val criticalDistanceMeters: Float = 3f
) {
    fun toExtrinsics(): CameraExtrinsics = CameraExtrinsics(
        heightMeters = cameraHeightMeters,
        pitchAngleDegrees = cameraTiltDegrees
    )
}

@Immutable
data class DetectionOverlayUiState(
    val id: Long,
    val label: String,
    val confidence: Float,
    val distanceMeters: Float,
    val normalizedBox: RectF
)

@Immutable
data class WarningBannerUiState(
    val title: String,
    val message: String,
    val critical: Boolean
)

@Immutable
data class AdasDashboardUiState(
    val isSettingsVisible: Boolean = false,
    val selectedTab: DashboardTab = DashboardTab.Screen,
    val metricVisibility: MetricVisibility = MetricVisibility(),
    val calibration: CalibrationUiState = CalibrationUiState(),
    val vehicleSpeedKmh: Float = 0f,
    val distanceMeters: Float = 0f,
    val latencyMs: Int = 0,
    val alertLevel: AlertLevel = AlertLevel.SAFE,
    val overlays: List<DetectionOverlayUiState> = emptyList(),
    val warningBanners: List<WarningBannerUiState> = emptyList()
)
