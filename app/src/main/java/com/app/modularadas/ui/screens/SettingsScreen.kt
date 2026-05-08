package com.app.modularadas.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.app.modularadas.ui.components.BackToCameraChip
import com.app.modularadas.ui.components.DashboardNavigationBar
import com.app.modularadas.ui.state.AdasDashboardUiState
import com.app.modularadas.ui.state.CalibrationUiState
import com.app.modularadas.ui.state.DashboardTab
import com.app.modularadas.ui.state.MetricVisibility

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun SettingsScreen(
    uiState: AdasDashboardUiState,
    onBack: () -> Unit,
    onTabSelected: (DashboardTab) -> Unit,
    onMetricVisibilityChange: (MetricVisibility) -> Unit,
    onCalibrationChange: ((CalibrationUiState) -> CalibrationUiState) -> Unit,
    onResetCalibration: () -> Unit = {},
    onCalibrateFromDetected: (Float) -> Unit = {},
    calibrationConfirmationMessage: String = "",
    onConfirmCalibration: (Float) -> Unit = {},
    onDismissCalibrationDialog: () -> Unit = {},
    calibrationTimestampMs: Long = 0L,
    computedFocalPx: Float = 0f,
    isCalibrationDialogVisible: Boolean = false
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val activity = context.findActivity()
        val previousOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        onDispose {
            previousOrientation?.let { activity?.requestedOrientation = it }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            DashboardNavigationBar(
                selectedTab = uiState.selectedTab,
                onTabSelected = { tab ->
                    // Only allow Screen and Calibration tabs; skip Network
                    if (tab != DashboardTab.Network) {
                        onTabSelected(tab)
                    }
                },
                modifier = Modifier.navigationBarsPadding(),
                hiddenTabs = listOf(DashboardTab.Network)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier.width(400.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BackToCameraChip(onClick = onBack)
                        if (uiState.selectedTab == DashboardTab.Calibration) {
                            TextButton(onClick = onResetCalibration) {
                                Text(
                                    text = "Reset",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = when (uiState.selectedTab) {
                            DashboardTab.Screen -> "Screen"
                            DashboardTab.Calibration -> "Calibration"
                            DashboardTab.Network -> "Network"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when (uiState.selectedTab) {
                            DashboardTab.Screen -> "Choose which metrics are displayed on the camera screen."
                            DashboardTab.Calibration -> "Calibrate the camera and vehicle parameters for accurate distance measurement."
                            DashboardTab.Network -> "Connect multiple phones via LAN for multi-angle monitoring."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    when (uiState.selectedTab) {
                        DashboardTab.Screen -> ScreenSettingsPane(
                            metricVisibility = uiState.metricVisibility,
                            onMetricVisibilityChange = onMetricVisibilityChange,
                            uiState = uiState
                        )
                        DashboardTab.Calibration -> CalibrationPane(
                            calibration = uiState.calibration,
                            overlays = uiState.overlays,
                            onCalibrationChange = onCalibrationChange,
                            onResetToDefaults = onResetCalibration,
                            onCalibrateFromDetected = onCalibrateFromDetected
                        )
                        DashboardTab.Network -> NetworkPane()
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }

    // Calibration Confirmation Dialog
    if (isCalibrationDialogVisible && calibrationConfirmationMessage.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = onDismissCalibrationDialog,
            title = { Text("Confirm Calibration") },
            text = { Text(calibrationConfirmationMessage) },
            confirmButton = {
                TextButton(onClick = { onConfirmCalibration(computedFocalPx) }) {
                    Text("Accept")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissCalibrationDialog) {
                    Text("Cancel")
                }
            }
        )
    }
}
