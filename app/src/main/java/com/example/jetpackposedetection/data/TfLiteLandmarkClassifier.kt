package com.example.jetpackposedetection.data

import android.R.attr
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import com.example.jetpackposedetection.LandmarkClassifier
import com.example.jetpackposedetection.landmarks
import com.example.jetpackposedetection.ml.Mediapipe
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer


class TfLiteLandmarkClassifier(
    private val context : Context,
) : LandmarkClassifier {
    private var model : Mediapipe = Mediapipe.newInstance(context)
    private var imageProcessor: ImageProcessor = ImageProcessor
        .Builder()
        .add(ResizeOp(256, 256, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()



    override fun classify(bitmap: Bitmap, rotationDegrees: Int): List<landmarks> {
        val rotatedBitmap = rotateBitmap(bitmap, rotationDegrees , false , false)
        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(rotatedBitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 256 , 256 , 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(tensorImage.buffer)
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray
        val width  = rotatedBitmap.width
        val height = rotatedBitmap.height
        Log.d("bitmap", "$width and $height")
        val landmarksList = outputFeature0.toList()
            .chunked(5) { chunk ->
                landmarks(
                    (chunk[0]/255f),
                    (chunk[1]/255f),
                    chunk[2],
                    chunk[3],
                    chunk[4]
                )
            }
        return landmarksList
    }
    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int, flipX : Boolean, flipY : Boolean): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
        val rotatedBitmap: Bitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
        }
        return rotatedBitmap ;
    }
}