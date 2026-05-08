package com.app.modularadas.ui.screens

import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.modularadas.ui.state.AdasDashboardUiState
import com.app.modularadas.ui.viewmodel.AdasDashboardViewModel

/**
 * Top-level route composable for the ADAS Dashboard screen.
 * Manages navigation between the main camera screen and settings screens.
 * Delegates all screen content to focused, purpose-built composables.
 */
@Composable
fun AdasDashboardRoute(
    viewModel: AdasDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val calibrationConfirmation by viewModel.calibrationConfirmation.collectAsStateWithLifecycle()

    if (uiState.isSettingsVisible) {
        SettingsScreen(
            uiState = uiState,
            onBack = viewModel::closeSettings,
            onTabSelected = viewModel::selectTab,
            onMetricVisibilityChange = viewModel::setMetricVisibility,
            onCalibrationChange = viewModel::updateCalibration,
            onResetCalibration = viewModel::resetCalibrationToDefaults,
            onCalibrateFromDetected = viewModel::calibrateFocalFromReference,
            calibrationConfirmationMessage = calibrationConfirmation.message,
            onConfirmCalibration = viewModel::confirmCalibration,
            onDismissCalibrationDialog = viewModel::dismissCalibrationConfirmation,
            isCalibrationDialogVisible = calibrationConfirmation.isVisible,
            computedFocalPx = calibrationConfirmation.computedFocalPx
        )
    } else {
        MainCameraScreen(
            uiState = uiState,
            onSettingsClick = viewModel::openSettings,
            onPreviewReady = viewModel::startCamera,
            onPreviewClosed = viewModel::stopCamera
        )
    }
}
