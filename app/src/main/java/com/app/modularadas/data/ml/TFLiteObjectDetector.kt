package com.app.modularadas.data.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import com.app.modularadas.domain.repository.ObjectDetector
import com.app.modularadas.domain.repository.RawDetection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector as TFLiteDetector
import org.tensorflow.lite.task.vision.detector.ObjectDetector.ObjectDetectorOptions

/**
 * Concrete implementation of the ML Engine using TensorFlow Lite Task Vision.
 * Responsible for GPU delegation and tensor manipulation.
 */
class TFLiteObjectDetector(
    private val context: Context,
    private val modelPath: String = "yolov10n_float16.tflite" 
) : ObjectDetector {
    companion object {
        private const val TAG = "TFLiteDetector"
    }

    private var detector: TFLiteDetector? = null
    private var gpuDelegateEnabled = false

    val isReady: Boolean
        get() = detector != null

    init {
        setupDetector()
    }

    private fun setupDetector() {
        Log.d(TAG, "[Detector Setup] Initializing TFLite Object Detector...")
        val optionsBuilder = ObjectDetectorOptions.builder()
            .setMaxResults(10)
            .setScoreThreshold(0.4f) // Filter out low-confidence noise immediately

        val baseOptionsBuilder = BaseOptions.builder()

        // Safety check: Ensure the device's GPU supports the necessary OpenCL/OpenGL 
        gpuDelegateEnabled = CompatibilityList().isDelegateSupportedOnThisDevice
        if (gpuDelegateEnabled) {
            Log.d(TAG, "[Detector Setup] GPU delegate ENABLED on this device")
            baseOptionsBuilder.useGpu()
        } else {
            // Fallback to NNAPI or multi-threading if GPU delegate fails
            Log.d(TAG, "[Detector Setup] GPU delegate NOT supported, using 4 threads instead")
            baseOptionsBuilder.setNumThreads(4)
        }

        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        try {
            detector = TFLiteDetector.createFromFileAndOptions(context, modelPath, optionsBuilder.build())
            Log.d(TAG, "[Detector Setup] SUCCESS: Detector initialized. Model: $modelPath")
        } catch (e: Exception) {
            Log.e(TAG, "[Detector Setup] FAILED: Could not load model at $modelPath", e)
            e.printStackTrace()
        }
    }

    override suspend fun detect(frame: Bitmap): List<RawDetection> = withContext(Dispatchers.Default) {
        // ============ GUARD CHECK 1: Detector readiness ============
        val currentDetector = detector
        if (currentDetector == null) {
            Log.w(TAG, "[Guard Check] Detector not initialized (null). Returning empty detections.")
            return@withContext emptyList()
        }
        if (!isReady) {
            Log.w(TAG, "[Guard Check] Detector reports not ready (!isReady). Returning empty detections.")
            return@withContext emptyList()
        }
        Log.d(TAG, "[Detect] Detector is ready (GPU=$gpuDelegateEnabled). Processing frame: ${frame.width}x${frame.height}")

        // Convert the Android Bitmap into a TFLite TensorImage
        val tensorImage = TensorImage.fromBitmap(frame)
        Log.d(TAG, "[Detect] TensorImage created successfully")

        // Run synchronous inference on the background dispatcher
        val results = currentDetector.detect(tensorImage)
        Log.d(TAG, "[Detect] Raw TFLite detections count: ${results.size}")
        
        if (results.isEmpty()) {
            Log.d(TAG, "[Detect] No detections from TFLite model (empty results)")
            return@withContext emptyList()
        }

        // Log all raw detections for diagnostics
        results.forEachIndexed { idx, detection ->
            val category = detection.categories.firstOrNull()
            Log.d(TAG, "[Detect] Raw[#$idx] label=${category?.label ?: "NONE"} score=${category?.score ?: "N/A"} box=${detection.boundingBox}")
        }

        // Map the proprietary TFLite Detection objects to our pure Domain models
        val filtered = results.mapNotNull { detection ->
            val category = detection.categories.firstOrNull()
            
            // ============ GUARD CHECK 2: Category exists ============
            if (category == null) {
                Log.d(TAG, "[Guard Check] Detection has no category, skipping")
                return@mapNotNull null
            }
            
            // Only care about vehicular or pedestrian obstacles for Phase rear-collision
            val allowedLabels = listOf("car", "truck", "bus", "motorcycle", "person")
            if (category.label !in allowedLabels) {
                Log.d(TAG, "[Filter] Skipping label '${category.label}' (not in allowed list: $allowedLabels)")
                return@mapNotNull null
            }

            Log.d(TAG, "[Filter] Accepted detection: label='${category.label}' confidence=${category.score}")
            RawDetection(
                boundingBox = detection.boundingBox,
                label = category.label,
                confidence = category.score
            )
        }
        
        Log.d(TAG, "[Detect] Final filtered detections count: ${filtered.size} (from ${results.size} raw)")
        filtered
    }

    fun close() {
        Log.d(TAG, "[Detector] Closing detector")
        detector?.close()
        detector = null
    }
}