package com.example.jetpackposedetection.data

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.pose.Pose

interface LandmarkClassifier {
    fun classify(bitmap : Bitmap, rotationDegrees : Int) : List<Landmarks>
    fun classifyMlKit(image : ImageProxy , onResults :(Pose?) -> Unit)
}