package com.app.modularadas.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.app.modularadas.ui.state.CalibrationUiState
import com.app.modularadas.ui.state.MetricVisibility
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "adas_settings")

class SettingsRepository(private val context: Context) {
    
    // MetricVisibility Keys
    private val metricSpeedKey = booleanPreferencesKey("metric_speed")
    private val metricDistanceKey = booleanPreferencesKey("metric_distance")
    private val metricLatencyKey = booleanPreferencesKey("metric_latency")
    
    // CalibrationUiState Keys
    private val cameraHeightKey = floatPreferencesKey("camera_height")
    private val cameraTiltKey = floatPreferencesKey("camera_tilt")
    private val focalLengthKey = floatPreferencesKey("focal_length")
    private val focalPxKey = floatPreferencesKey("focal_px")
    private val calibrationTimestampKey = floatPreferencesKey("calibration_timestamp")
    private val vehicleWidthKey = floatPreferencesKey("vehicle_width")
    private val referenceDistanceKey = floatPreferencesKey("reference_distance")
    private val warningDistanceKey = floatPreferencesKey("warning_distance")
    private val criticalDistanceKey = floatPreferencesKey("critical_distance")

    // Load MetricVisibility from DataStore
    fun getMetricVisibility(): Flow<MetricVisibility> {
        return context.dataStore.data.map { preferences ->
            MetricVisibility(
                speed = preferences[metricSpeedKey] ?: true,
                distance = preferences[metricDistanceKey] ?: true,
                latency = preferences[metricLatencyKey] ?: true
            )
        }
    }

    // Save MetricVisibility to DataStore
    suspend fun saveMetricVisibility(visibility: MetricVisibility) {
        context.dataStore.edit { preferences ->
            preferences[metricSpeedKey] = visibility.speed
            preferences[metricDistanceKey] = visibility.distance
            preferences[metricLatencyKey] = visibility.latency
        }
    }

    // Load CalibrationUiState from DataStore
    fun getCalibration(): Flow<CalibrationUiState> {
        return context.dataStore.data.map { preferences ->
            CalibrationUiState(
                cameraHeightMeters = preferences[cameraHeightKey] ?: 1.5f,
                cameraTiltDegrees = preferences[cameraTiltKey] ?: 15f,
                focalLengthMm = preferences[focalLengthKey] ?: 35f,
                focalPx = preferences[focalPxKey] ?: 0f,
                vehicleWidthMeters = preferences[vehicleWidthKey] ?: 1.9f,
                referenceDistanceMeters = preferences[referenceDistanceKey] ?: 3f,
                warningDistanceMeters = preferences[warningDistanceKey] ?: 8f,
                criticalDistanceMeters = preferences[criticalDistanceKey] ?: 3f,
                calibrationTimestampMs = preferences[calibrationTimestampKey]?.toLong() ?: 0L
            )
        }
    }

    // Save CalibrationUiState to DataStore
    suspend fun saveCalibration(calibration: CalibrationUiState) {
        context.dataStore.edit { preferences ->
            preferences[cameraHeightKey] = calibration.cameraHeightMeters
            preferences[cameraTiltKey] = calibration.cameraTiltDegrees
            preferences[focalLengthKey] = calibration.focalLengthMm
            preferences[focalPxKey] = calibration.focalPx
            preferences[vehicleWidthKey] = calibration.vehicleWidthMeters
            preferences[referenceDistanceKey] = calibration.referenceDistanceMeters
            preferences[warningDistanceKey] = calibration.warningDistanceMeters
            preferences[criticalDistanceKey] = calibration.criticalDistanceMeters
            preferences[calibrationTimestampKey] = calibration.calibrationTimestampMs.toFloat()
        }
    }
}
