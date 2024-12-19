package com.example.jetpackposedetection.data

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.jetpackposedetection.utils.BitmapUtils
import com.example.jetpackposedetection.painter.GraphicOverlay
import com.google.mlkit.vision.pose.Pose

class LandmarkImageAnalyzer(
    cameraSelector: Int,
    private val classifier: LandmarkClassifier,
    private val onResults: ( bitmap : Bitmap , Pose?) -> Unit,
    private val graphicOverlay: GraphicOverlay
): ImageAnalysis.Analyzer {
    @OptIn(ExperimentalGetImage::class)
    private var bitmap: Bitmap? = null
    private var lensFacing: Int = cameraSelector
    private var needUpdateGraphicOverlayImageSourceInfo: Boolean = true
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image:ImageProxy) {
        if (needUpdateGraphicOverlayImageSourceInfo) {
            val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
            Log.d("CameraViewModel", "isImageFlipped: $isImageFlipped")
            val rotationDegrees = image.imageInfo.rotationDegrees
            Log.d("CameraViewModel", "rotationDegrees: $rotationDegrees")
            if (rotationDegrees == 0 || rotationDegrees == 180){
                graphicOverlay.setImageSourceInfo(image.width, image.height, isImageFlipped)
            } else {
                graphicOverlay.setImageSourceInfo(image.height, image.width, isImageFlipped)
            }
            needUpdateGraphicOverlayImageSourceInfo = false
        }
        classifier.classifyMlKit(image){results->
            bitmap = BitmapUtils.getBitmap(image, graphicOverlay)
            if (bitmap != null) {
                if (results != null) {
                    onResults(bitmap!!, results)
                }
            }
        }
    }
}