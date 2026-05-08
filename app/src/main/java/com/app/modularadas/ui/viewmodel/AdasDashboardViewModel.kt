package com.app.modularadas.ui.viewmodel

import android.app.Application
import android.graphics.RectF
import android.os.SystemClock
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import com.app.modularadas.core.constants.AdasConfig
import com.app.modularadas.data.camera.CameraController
import com.app.modularadas.data.ml.TFLiteObjectDetector
import com.app.modularadas.data.settings.SettingsRepository
import com.app.modularadas.domain.models.AlertLevel
import com.app.modularadas.domain.usecase.EvaluateAlertUseCase
import com.app.modularadas.domain.usecase.ProcessFrameUseCase
import com.app.modularadas.ui.state.AdasDashboardUiState
import com.app.modularadas.ui.state.CalibrationUiState
import com.app.modularadas.ui.state.DashboardTab
import com.app.modularadas.ui.state.DetectionOverlayUiState
import com.app.modularadas.ui.state.MetricVisibility
import com.app.modularadas.ui.state.WarningBannerUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdasDashboardViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "AdasDashboardViewModel"
    }
    private val cameraController = CameraController(application.applicationContext)
    private val detector = TFLiteObjectDetector(application.applicationContext)
    private val processFrameUseCase = ProcessFrameUseCase(detector)
    private val evaluateAlertUseCase = EvaluateAlertUseCase()
    private val settingsRepository = SettingsRepository(application.applicationContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var processingJob: Job? = null
    private var overlayIdSeed: Long = 0L

    private val _uiState = MutableStateFlow(AdasDashboardUiState())
    val uiState: StateFlow<AdasDashboardUiState> = _uiState.asStateFlow()

    init {
        // Load persisted settings from DataStore
        scope.launch {
            // Load MetricVisibility
            settingsRepository.getMetricVisibility().collect { visibility ->
                _uiState.update { it.copy(metricVisibility = visibility) }
            }
        }
        scope.launch {
            // Load CalibrationUiState
            settingsRepository.getCalibration().collect { calibration ->
                _uiState.update { it.copy(calibration = calibration) }
            }
        }
    }

    fun openSettings() {
        _uiState.update { it.copy(isSettingsVisible = true) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(isSettingsVisible = false) }
    }

    fun selectTab(tab: DashboardTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setMetricVisibility(visibility: MetricVisibility) {
        _uiState.update { it.copy(metricVisibility = visibility) }
        // Persist to DataStore
        scope.launch {
            settingsRepository.saveMetricVisibility(visibility)
        }
    }

    fun updateCalibration(update: (CalibrationUiState) -> CalibrationUiState) {
        _uiState.update { current -> 
            val newCalibration = update(current.calibration)
            // Persist to DataStore
            scope.launch {
                settingsRepository.saveCalibration(newCalibration)
            }
            current.copy(calibration = newCalibration) 
        }
    }

    fun resetCalibrationToDefaults() {
        val defaultCalibration = CalibrationUiState()
        _uiState.update { it.copy(calibration = defaultCalibration) }
        // Persist to DataStore
        scope.launch {
            settingsRepository.saveCalibration(defaultCalibration)
        }
    }

    fun startCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        Log.d(TAG, "[startCamera] Starting camera and frame processing")
        
        // Guard check: Ensure detector is initialized
        if (!detector.isReady) {
            Log.e(TAG, "[startCamera] ERROR: Detector not ready at startup!")
        }
        
        cameraController.startCamera(
            lifecycleOwner = lifecycleOwner,
            surfaceProvider = previewView.surfaceProvider
        ) { frame, verticalFovDegrees ->
            if (processingJob?.isActive == true) {
                return@startCamera
            }
            processingJob = scope.launch {
                try {
                    val start = SystemClock.elapsedRealtime()
                    val currentCalibration = _uiState.value.calibration
                    
                    Log.d(TAG, "[frameProcessing] Starting frame analysis")
                    val processedDetections = processFrameUseCase(
                        frame = frame,
                        extrinsics = currentCalibration.toExtrinsics(),
                        verticalFovDegrees = verticalFovDegrees
                    )
                    val processingLatencyMs = (SystemClock.elapsedRealtime() - start).toInt()
                    
                    Log.d(TAG, "[frameProcessing] Detections: count=${processedDetections.size}, latency=${processingLatencyMs}ms")
                    
                    if (processedDetections.isNotEmpty()) {
                        processedDetections.forEach { det ->
                            Log.d(TAG, "[frameProcessing] Detection: label='${det.label}' confidence=${det.confidence} distance=${det.estimatedDistanceMeters}m")
                        }
                    } else {
                        Log.d(TAG, "[frameProcessing] No detections in this frame")
                    }
                    
                    val nearestDistance = processedDetections.minOfOrNull { it.estimatedDistanceMeters }
                    val alertLevel = evaluateAlertUseCase(processedDetections)
                    Log.d(TAG, "[frameProcessing] Alert level: $alertLevel, nearest distance: $nearestDistance m")
                    
                    val overlays = processedDetections.map { detection ->
                        DetectionOverlayUiState(
                            id = overlayIdSeed++,
                            label = detection.label,
                            confidence = detection.confidence,
                            distanceMeters = detection.estimatedDistanceMeters,
                            normalizedBox = RectF(
                                detection.boundingBox.left / frame.width.toFloat(),
                                detection.boundingBox.top / frame.height.toFloat(),
                                detection.boundingBox.right / frame.width.toFloat(),
                                detection.boundingBox.bottom / frame.height.toFloat()
                            )
                        )
                    }
                    Log.d(TAG, "[frameProcessing] Created overlay objects: count=${overlays.size}")

                    _uiState.update { current ->
                        val distanceMeters = nearestDistance ?: current.distanceMeters
                        val speedKmh = if (nearestDistance != null) {
                            val previousDistance = current.distanceMeters.takeIf { it > 0f }
                            if (previousDistance != null && processingLatencyMs > 0) {
                                val deltaMeters = previousDistance - distanceMeters
                                val seconds = processingLatencyMs / 1000f
                                ((deltaMeters / seconds) * 3.6f).coerceIn(0f, 140f)
                            } else {
                                current.vehicleSpeedKmh
                            }
                        } else {
                            0f
                        }
                        current.copy(
                            vehicleSpeedKmh = speedKmh,
                            distanceMeters = distanceMeters,
                            latencyMs = processingLatencyMs,
                            alertLevel = alertLevel,
                            overlays = overlays,
                            warningBanners = buildWarnings(distanceMeters, alertLevel)
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[frameProcessing] Exception during frame processing", e)
                }
            }
        }
    }

    fun stopCamera() {
        cameraController.stopCamera()
        processingJob?.cancel()
        processingJob = null
    }

    private fun buildWarnings(distanceMeters: Float, alertLevel: AlertLevel): List<WarningBannerUiState> {
        return when {
            alertLevel == AlertLevel.CRITICAL || distanceMeters <= AdasConfig.THRESHOLD_CRITICAL_METERS -> {
                listOf(
                    WarningBannerUiState(
                        title = "Warning!!!!",
                        message = "Stop the car to avoid Collision",
                        critical = true
                    )
                )
            }
            distanceMeters <= AdasConfig.THRESHOLD_WARNING_METERS -> {
                listOf(
                    WarningBannerUiState(
                        title = "Warning!",
                        message = "Car only ${distanceMeters.roundDisplay()} m away from obstacle",
                        critical = false
                    )
                )
            }
            else -> emptyList()
        }
    }

    private fun Float.roundDisplay(): String {
        val rounded = String.format(java.util.Locale.US, "%.1f", this)
        return if (rounded.endsWith(".0")) rounded.dropLast(2) else rounded
    }

    override fun onCleared() {
        stopCamera()
        detector.close()
        scope.cancel()
        super.onCleared()
    }
}
