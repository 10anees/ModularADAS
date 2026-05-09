package com.app.modularadas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.modularadas.ui.components.CalibrationSliderRow
import com.app.modularadas.ui.components.SettingsSectionTitle
import com.app.modularadas.ui.components.SummaryCard
import com.app.modularadas.ui.state.CalibrationUiState
import com.app.modularadas.ui.state.DetectionOverlayUiState

private fun formatValue(value: Float, maxDecimals: Int = 2): String {
    return String.format("%.${maxDecimals}f", value)
}

@Composable
fun CalibrationPane(
    calibration: CalibrationUiState,
    overlays: List<DetectionOverlayUiState> = emptyList(),
    onCalibrationChange: ((CalibrationUiState) -> CalibrationUiState) -> Unit,
    onResetToDefaults: () -> Unit = {},
    onCalibrateFromDetected: (Float) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SettingsSectionTitle(title = "Camera Parameters")
        CalibrationSliderRow(
            title = "Camera Mount Height",
            description = "Height of camera above ground level",
            value = calibration.cameraHeightMeters,
            valueLabel = "${formatValue(calibration.cameraHeightMeters)}m",
            range = 0.5f..3.0f,
            onValueChange = { value -> onCalibrationChange { it.copy(cameraHeightMeters = value) } }
        )
        CalibrationSliderRow(
            title = "Camera Tilt Angle",
            description = "Downward tilt angle of the camera lens",
            value = calibration.cameraTiltDegrees,
            valueLabel = "${formatValue(calibration.cameraTiltDegrees)}°",
            range = 0f..45f,
            onValueChange = { value -> onCalibrationChange { it.copy(cameraTiltDegrees = value) } }
        )
        CalibrationSliderRow(
            title = "Focal Length",
            description = "Camera lens focal length",
            value = calibration.focalLengthMm,
            valueLabel = "${formatValue(calibration.focalLengthMm)}mm",
            range = 15f..70f,
            onValueChange = { value -> onCalibrationChange { it.copy(focalLengthMm = value) } }
        )

        SettingsSectionTitle(title = "Vehicle Parameters")
        CalibrationSliderRow(
            title = "Vehicle Width",
            description = "Width of your vehicle for scale reference",
            value = calibration.vehicleWidthMeters,
            valueLabel = "${formatValue(calibration.vehicleWidthMeters)}m",
            range = 1.4f..2.5f,
            onValueChange = { value -> onCalibrationChange { it.copy(vehicleWidthMeters = value) } }
        )
        CalibrationSliderRow(
            title = "Reference Distance",
            description = "Known distance used to calibrate scale",
            value = calibration.referenceDistanceMeters,
            valueLabel = "${formatValue(calibration.referenceDistanceMeters)}m",
            range = 1f..10f,
            onValueChange = { value -> onCalibrationChange { it.copy(referenceDistanceMeters = value) } }
        )

        // Calibrate from detected object button
        if (overlays.isNotEmpty()) {
            SettingsSectionTitle(title = "Auto-Calibration")
            val largestOverlay = overlays.maxByOrNull { overlay ->
                (overlay.normalizedBox.right - overlay.normalizedBox.left)
            }
            val detectedLabel = largestOverlay?.label ?: "object"
            val pixelWidth = largestOverlay?.let { overlay ->
                // Assuming frame width of 1080 as typical (can be parameterized if needed)
                (overlay.normalizedBox.right - overlay.normalizedBox.left) * 1080f
            } ?: 0f

            Button(
                onClick = {
                    if (pixelWidth > 0f) {
                        onCalibrateFromDetected(pixelWidth)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = pixelWidth > 0f,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            ) {
                Text(
                    text = if (pixelWidth > 0f) {
                        String.format("Calibrate from Detected Object — %s (%.0f px)", detectedLabel, pixelWidth)
                    } else {
                        "Calibrate from Detected Object (no detections)"
                    }
                )
            }
        }

        SettingsSectionTitle(title = "Warning Thresholds")
        CalibrationSliderRow(
            title = "Caution Distance",
            description = "Yellow warning triggers below this distance",
            value = calibration.warningDistanceMeters,
            valueLabel = "${formatValue(calibration.warningDistanceMeters)}m",
            range = 2f..20f,
            onValueChange = { value -> onCalibrationChange { it.copy(warningDistanceMeters = value) } }
        )
        CalibrationSliderRow(
            title = "Critical Distance",
            description = "Red warning triggers below this distance",
            value = calibration.criticalDistanceMeters,
            valueLabel = "${formatValue(calibration.criticalDistanceMeters)}m",
            range = 1f..8f,
            onValueChange = { value -> onCalibrationChange { it.copy(criticalDistanceMeters = value) } }
        )

        SummaryCard(calibration = calibration)
    }
}
