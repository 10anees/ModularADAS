package com.app.modularadas.ui.components

import android.graphics.Paint
import android.graphics.RectF as AndroidRectF
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.DoorFront
import androidx.compose.material.icons.outlined.LensBlur
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.modularadas.ui.state.CalibrationUiState
import com.app.modularadas.ui.state.DashboardTab
import com.app.modularadas.ui.state.DetectionOverlayUiState
import com.app.modularadas.ui.state.MetricVisibility
import com.app.modularadas.ui.state.WarningBannerUiState
import kotlin.math.roundToInt

private fun formatValue(value: Float, maxDecimals: Int = 2): String {
    return String.format("%.${maxDecimals}f", value)
}

@Composable
fun MetricCircle(
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    primary: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    lineHeight = 18.sp
                )
                Text(
                    text = unit,
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun LiveMetricRail(
    speedVisible: Boolean,
    distanceVisible: Boolean,
    latencyVisible: Boolean,
    speedKmh: Float,
    distanceMeters: Float,
    latencyMs: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(34.dp)
    ) {
        if (speedVisible) MetricCircle(value = speedKmh.roundToInt().toString(), unit = "km/h")
        if (distanceVisible) MetricCircle(value = distanceMeters.roundToInt().toString(), unit = "m", primary = false)
        if (latencyVisible) MetricCircle(value = latencyMs.toString(), unit = "ms")
    }
}

@Composable
fun SettingsFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.size(44.dp),
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = Color.White
            )
        }
    }
}

@Composable
fun WarningBanner(
    warning: WarningBannerUiState,
    modifier: Modifier = Modifier
) {
    val background = if (warning.critical) Color(0xFFFF5252) else Color(0xFFF6C04E)
    val contentColor = Color(0xFF141414)
    Surface(
        modifier = modifier,
        color = background,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = warning.title,
                    color = contentColor,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = warning.message,
                    color = contentColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun DetectionOverlayCanvas(
    detections: List<DetectionOverlayUiState>,
    modifier: Modifier = Modifier
) {
    val overlayColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
    val density = LocalDensity.current
    val textSizePx = with(density) { 11.sp.toPx() }
    val horizontalPadPx = with(density) { 6.dp.toPx() }
    val verticalPadPx = with(density) { 4.dp.toPx() }
    val badgeGapPx = with(density) { 4.dp.toPx() }
    val textYOffsetPx = with(density) { 2.dp.toPx() }
    val textPaint = remember(textSizePx) {
        Paint().apply {
            color = Color.White.toArgb()
            textSize = textSizePx
            isAntiAlias = true
            isFakeBoldText = true
        }
    }
    val badgePaint = remember(overlayColor) {
        Paint().apply {
            color = overlayColor.toArgb()
            isAntiAlias = true
        }
    }

    Canvas(modifier = modifier) {
        detections.forEach { detection ->
            val left = detection.normalizedBox.left * size.width
            val top = detection.normalizedBox.top * size.height
            val width = (detection.normalizedBox.right - detection.normalizedBox.left) * size.width
            val height = (detection.normalizedBox.bottom - detection.normalizedBox.top) * size.height
            val confidencePct = (detection.confidence * 100f).roundToInt()
            val distanceText = String.format(java.util.Locale.US, "%.1fm", detection.distanceMeters)
            val labelText = "${detection.label} ${confidencePct}% ${distanceText}"
            val badgeWidth = textPaint.measureText(labelText) + (horizontalPadPx * 2f)
            val badgeHeight = textSizePx + (verticalPadPx * 2f)
            val maxBadgeLeft = (size.width - badgeWidth).coerceAtLeast(0f)
            val badgeLeft = left.coerceIn(0f, maxBadgeLeft)
            val badgeTop = (top - badgeHeight - badgeGapPx).coerceAtLeast(0f)

            drawRoundRect(
                color = overlayColor,
                topLeft = Offset(left, top),
                size = Size(width, height),
                style = Stroke(
                    width = 2.2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f), 0f)
                )
            )

            val nativeCanvas = drawContext.canvas.nativeCanvas
            nativeCanvas.drawRoundRect(
                AndroidRectF(
                    badgeLeft,
                    badgeTop,
                    badgeLeft + badgeWidth,
                    badgeTop + badgeHeight
                ),
                8f,
                8f,
                badgePaint
            )
            nativeCanvas.drawText(
                labelText,
                badgeLeft + horizontalPadPx,
                badgeTop + badgeHeight - verticalPadPx - textYOffsetPx,
                textPaint
            )
        }
    }
}

@Composable
fun DashboardNavigationBar(
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit,
    modifier: Modifier = Modifier,
    hiddenTabs: List<DashboardTab> = emptyList()
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 8.dp
    ) {
        if (!hiddenTabs.contains(DashboardTab.Screen)) {
            NavigationBarItem(
                selected = selectedTab == DashboardTab.Screen,
                onClick = { onTabSelected(DashboardTab.Screen) },
                icon = { Icon(Icons.Filled.Speed, contentDescription = null) },
                label = { Text("Screen") }
            )
        }
        if (!hiddenTabs.contains(DashboardTab.Calibration)) {
            NavigationBarItem(
                selected = selectedTab == DashboardTab.Calibration,
                onClick = { onTabSelected(DashboardTab.Calibration) },
                icon = { Icon(Icons.Outlined.Straighten, contentDescription = null) },
                label = { Text("Calibration") }
            )
        }
        if (!hiddenTabs.contains(DashboardTab.Network)) {
            NavigationBarItem(
                selected = selectedTab == DashboardTab.Network,
                onClick = { onTabSelected(DashboardTab.Network) },
                icon = { Icon(Icons.Outlined.DoorFront, contentDescription = null) },
                label = { Text("Network") }
            )
        }
    }
}

@Composable
fun BackToCameraChip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Back to Camera",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
fun ScreenToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.DirectionsCar,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun CalibrationSliderRow(
    title: String,
    description: String,
    value: Float,
    valueLabel: String,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                AssistChip(onClick = {}, label = { Text(valueLabel) })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range.start..range.endInclusive
            )
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.2.sp
    )
}

@Composable
fun SummaryCard(
    calibration: CalibrationUiState,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(text = "Calibration Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryValue(label = "Height", value = "${formatValue(calibration.cameraHeightMeters)}m")
                SummaryValue(label = "Tilt", value = "${formatValue(calibration.cameraTiltDegrees)}°")
                SummaryValue(label = "Focal", value = "${formatValue(calibration.focalLengthMm)}mm")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryValue(label = "Vehicle", value = "${formatValue(calibration.vehicleWidthMeters)}m")
                SummaryValue(label = "Caution", value = "${formatValue(calibration.warningDistanceMeters)}m")
                SummaryValue(label = "Critical", value = "${formatValue(calibration.criticalDistanceMeters)}m")
            }
        }
    }
}

@Composable
private fun SummaryValue(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
