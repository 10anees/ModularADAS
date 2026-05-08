package com.app.modularadas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.modularadas.ui.components.MetricCircle
import com.app.modularadas.ui.components.ScreenToggleRow
import com.app.modularadas.ui.components.SettingsSectionTitle
import com.app.modularadas.ui.state.AdasDashboardUiState
import com.app.modularadas.ui.state.MetricVisibility

@Composable
fun ScreenSettingsPane(
    metricVisibility: MetricVisibility,
    onMetricVisibilityChange: (MetricVisibility) -> Unit,
    uiState: AdasDashboardUiState
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SettingsSectionTitle(title = "Screen")
        ScreenToggleRow(
            title = "Speed",
            description = "Show current vehicle speed (km/h)",
            checked = metricVisibility.speed,
            onCheckedChange = { onMetricVisibilityChange(metricVisibility.copy(speed = it)) }
        )
        ScreenToggleRow(
            title = "Distance",
            description = "Show distance to nearest obstacle (m)",
            checked = metricVisibility.distance,
            onCheckedChange = { onMetricVisibilityChange(metricVisibility.copy(distance = it)) }
        )
        ScreenToggleRow(
            title = "Latency",
            description = "Show processing latency (ms)",
            checked = metricVisibility.latency,
            onCheckedChange = { onMetricVisibilityChange(metricVisibility.copy(latency = it)) }
        )

        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Preview", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (metricVisibility.speed) {
                        MetricCircle(value = uiState.vehicleSpeedKmh.toInt().toString(), unit = "km/h")
                    }
                    if (metricVisibility.distance) {
                        MetricCircle(value = uiState.distanceMeters.toInt().toString(), unit = "m", primary = false)
                    }
                    if (metricVisibility.latency) {
                        MetricCircle(value = uiState.latencyMs.toString(), unit = "ms")
                    }
                }
            }
        }
    }
}
