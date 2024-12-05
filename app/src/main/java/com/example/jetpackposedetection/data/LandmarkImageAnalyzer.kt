package com.example.jetpackposedetection.data

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.jetpackposedetection.LandmarkClassifier
import com.example.jetpackposedetection.landmarks

class LandmarkImageAnalyzer(
    private val classifier: LandmarkClassifier,
    private val onResults: (List<landmarks>) -> Unit
): ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
      val rotationDegrees = image.imageInfo.rotationDegrees
      val bitmap = image.toBitmap()
        val result = classifier.classify(bitmap, rotationDegrees)
        onResults(result)
      image.close()
    }
}