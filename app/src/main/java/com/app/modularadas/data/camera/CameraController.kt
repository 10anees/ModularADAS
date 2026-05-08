package com.app.modularadas.data.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.app.modularadas.core.util.ImageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Manages the CameraX lifecycle, Preview surface, and high-speed frame extraction.
 */
class CameraController(
    private val context: Context,
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
) {

    private var imageAnalyzer: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var verticalFovDegrees: Float = 45f // Default, updated on bind

    /**
     * Binds the camera to the lifecycle of the Compose screen.
     * 
     * @param lifecycleOwner The active screen lifecycle.
     * @param surfaceProvider The Compose AndroidView surface for the live viewfinder.
     * @param onFrameExtracted Callback pushing the converted Bitmap to the ViewModel/UseCase.
     */
    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        onFrameExtracted: (Bitmap, Float) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(surfaceProvider) }

            imageAnalyzer = ImageAnalysis.Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                // ADAS Rule: Always drop delayed frames to prevent latency drift
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy, onFrameExtracted)
                    }
                }

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner, 
                    cameraSelector, 
                    preview, 
                    imageAnalyzer
                )

            } catch (exc: Exception) {
                Log.e("CameraController", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun processImageProxy(
        imageProxy: ImageProxy, 
        onFrameExtracted: (Bitmap, Float) -> Unit
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val bitmap = ImageUtils.yuv420ToBitmap(mediaImage)

        if (bitmap != null) {
            // Correct rotation based on device sensor orientation
            val rotatedBitmap = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees)
            onFrameExtracted(rotatedBitmap, verticalFovDegrees)
        }

        imageProxy.close()
    }

    private fun rotateBitmap(source: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return source
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun shutdown() {
        stopCamera()
        cameraExecutor.shutdown()
    }

    fun stopCamera() {
        imageAnalyzer?.clearAnalyzer()
        imageAnalyzer = null
        cameraProvider?.unbindAll()
        cameraProvider = null
    }
}