package com.example.jetpackposedetection

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
                        val width : Float  = size.width
                        val height :Float = size.height
                        val previewRatio  : Float = 640f/480f
                        val screenRatio : Float = height/width
                        Log.d("canvas size" , "$width and $height")
                        results.value.forEach { landmark ->
                            var dx : Float = landmark.x
                            var dy : Float = landmark.y
                            if(screenRatio > previewRatio) {
                                val scaleX = height/640*480
                                val difX = (scaleX - width)/scaleX
                                dx = (dx-difX/2)*scaleX
                                dy *= height
                            }else{
                                val scaleY = width/640*480
                                val difY = (scaleY - height)/scaleY
                                dx *= width
                                dy = (dy-difY/2)*scaleY
                            }
                            drawCircle(
                                color = Color.Red,
                                radius = 10f,
                                center = Offset( dx,dy)
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

