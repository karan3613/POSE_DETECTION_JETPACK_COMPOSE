package com.example.jetpackposedetection

import android.graphics.Bitmap

interface LandmarkClassifier {
    fun classify(bitmap : Bitmap, rotationDegrees : Int) : List<landmarks>
}