package com.example.jetpackposedetection

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.example.jetpackposedetection.data.LandmarkImageAnalyzer
import com.example.jetpackposedetection.data.TfLiteLandmarkClassifier
import com.example.jetpackposedetection.ui.theme.JetpackPoseDetectionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            get_permissions()
            JetpackPoseDetectionTheme {

                val results = remember {
                    mutableStateOf(emptyList<landmarks>())
                }

                val analyzer = remember {
                    LandmarkImageAnalyzer(
                        classifier = TfLiteLandmarkClassifier(applicationContext),
                        onResults = {
                            results.value = it
                        }
                    )
                }
                val controller  = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(
                            LifecycleCameraController.IMAGE_ANALYSIS
                        )
                        setImageAnalysisAnalyzer(
                            ContextCompat.getMainExecutor(applicationContext) ,
                            analyzer
                        )
                    }
                }
                Box(
                    modifier = Modifier.fillMaxSize()
                ){
                    CameraPreview(controller , Modifier.fillMaxSize())

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        results.value.forEach { landmark ->
                            drawCircle(
                                color = Color.Red,
                                radius = 10f,
                                center = Offset( landmark.x * width , landmark.y * height)
                            )
                        }
                    }
                }
            }
        }
    }
    fun get_permissions(){
        if(checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] != PackageManager.PERMISSION_GRANTED) get_permissions()
    }
}

