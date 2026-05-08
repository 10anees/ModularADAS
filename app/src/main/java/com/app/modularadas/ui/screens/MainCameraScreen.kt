package com.app.modularadas.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import android.content.pm.ActivityInfo
import kotlinx.coroutines.delay
import com.app.modularadas.ui.components.DetectionOverlayCanvas
import com.app.modularadas.ui.components.LiveMetricRail
import com.app.modularadas.ui.components.SettingsFloatingButton
import com.app.modularadas.ui.components.WarningBanner
import com.app.modularadas.ui.state.AdasDashboardUiState

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun MainCameraScreen(
    uiState: AdasDashboardUiState,
    onSettingsClick: () -> Unit,
    onPreviewReady: (LifecycleOwner, PreviewView) -> Unit,
    onPreviewClosed: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var chromeVisible by rememberSaveable { mutableStateOf(false) }
    var interactionTick by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val activity = context.findActivity()
        val previousOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            previousOrientation?.let { activity?.requestedOrientation = it }
        }
    }

    LaunchedEffect(interactionTick) {
        if (chromeVisible) {
            delay(5000)
            chromeVisible = false
        }
    }

    DisposableEffect(previewView, lifecycleOwner) {
        val currentPreviewView = previewView
        if (currentPreviewView != null) {
            onPreviewReady(lifecycleOwner, currentPreviewView)
        }
        onDispose {
            onPreviewClosed()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                while (true) {
                    val event = awaitPointerEventScope { awaitPointerEvent() }
                    if (event.changes.any { it.changedToDownIgnoreConsumed() }) {
                        chromeVisible = true
                        interactionTick += 1
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .align(Alignment.Center)
        ) {
            AndroidView(
                factory = { context ->
                    PreviewView(context).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        previewView = this
                    }
                },
                modifier = Modifier.matchParentSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.15f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f)
                            )
                        )
                    )
            )

            DetectionOverlayCanvas(
                detections = uiState.overlays,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 18.dp, start = 18.dp, end = 18.dp, bottom = 20.dp)
            ) {
                LiveMetricRail(
                    speedVisible = uiState.metricVisibility.speed,
                    distanceVisible = uiState.metricVisibility.distance,
                    latencyVisible = uiState.metricVisibility.latency,
                    speedKmh = uiState.vehicleSpeedKmh,
                    distanceMeters = uiState.distanceMeters,
                    latencyMs = uiState.latencyMs,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                AnimatedVisibility(
                    visible = chromeVisible,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 60.dp, end = 18.dp),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    SettingsFloatingButton(
                        onClick = onSettingsClick
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    uiState.warningBanners.forEach { warning ->
                        WarningBanner(warning = warning, modifier = Modifier.fillMaxWidth(0.65f))
                    }
                }
            }
        }
    }
}
