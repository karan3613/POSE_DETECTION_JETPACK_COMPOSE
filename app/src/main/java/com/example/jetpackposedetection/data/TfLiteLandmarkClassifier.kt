package com.example.jetpackposedetection.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.example.jetpackposedetection.ml.Mediapipe
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer


class TfLiteLandmarkClassifier(
    private val context : Context,
) : LandmarkClassifier {
    private val model : Mediapipe = Mediapipe.newInstance(context)
    private var imageProcessor: ImageProcessor = ImageProcessor
        .Builder()
        .add(ResizeOp(256, 256, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()
    private val poseDetector: PoseDetector
    private var toggleRecording = false
    private var completeRecording = false

    init {
        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        poseDetector = PoseDetection.getClient(options)
    }

    override fun classify(bitmap: Bitmap, rotationDegrees: Int): List<Landmarks> {
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
                Landmarks(
                    (chunk[0]/255f),
                    (chunk[1]/255f),
                    chunk[2],
                    chunk[3],
                    chunk[4]
                )
            }
        return landmarksList
    }

    @OptIn(ExperimentalGetImage::class)
    override fun classifyMlKit(image : ImageProxy , onResults : (Pose?) -> Unit){
        val inputImage = InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees)
        poseDetector.process(inputImage)
            .addOnSuccessListener{pose->
                analyzePose(
                    pose = pose ,
                    onDetection = {toggle , complete->
                        toggleRecording = toggle
                        completeRecording = complete
                    }
                )
                onResults(pose)
            }.addOnCompleteListener{
                image.close()
            }
    }
    private fun analyzePose(pose : Pose , onDetection : (Boolean , Boolean) ->(Unit)){

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
        return rotatedBitmap
    }
}