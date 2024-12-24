package com.example.jetpackposedetection

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.AudioConfig
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jetpackposedetection.data.LandmarkImageAnalyzer
import com.example.jetpackposedetection.data.TfLiteLandmarkClassifier
import com.example.jetpackposedetection.ui.theme.JetpackPoseDetectionTheme
import com.example.jetpackposedetection.painter.GraphicOverlay
import com.example.jetpackposedetection.painter.PoseGraphic
import com.google.mlkit.vision.pose.Pose
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var recording : Recording? = null
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(
                this, CAMERAX_PERMISSIONS, 0
            )
        }
        setContent {

            JetpackPoseDetectionTheme {
                val results = remember { mutableStateOf<Pose?>(null) }
                val graphicOverlay = remember { GraphicOverlay() }
                val bitmapImage = remember { mutableStateOf<Bitmap?>(null) }
                val cameraSelector: MutableState<Int> = remember {
                    mutableIntStateOf(CameraSelector.LENS_FACING_BACK)
                }
                val viewModel : CameraViewModel = hiltViewModel()
                val analyzer = remember {
                    LandmarkImageAnalyzer(
                        classifier = TfLiteLandmarkClassifier(applicationContext),
                        onResults = { bitmap, pose ->
                            bitmapImage.value?.recycle()
                            bitmapImage.value = bitmap
                            results.value = pose
                        },
                        cameraSelector = cameraSelector.value ,
                        graphicOverlay = graphicOverlay
                    )
                }
                val preferredQuality = Quality.SD
                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.from(
                            preferredQuality,
                            FallbackStrategy.higherQualityOrLowerThan(preferredQuality)
                        )
                    )
                    .build()
                val videoCapture = VideoCapture.withOutput(recorder)

                val controller  = remember {
                        LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(
                            LifecycleCameraController.IMAGE_ANALYSIS or LifecycleCameraController.VIDEO_CAPTURE
                        )
                        setImageAnalysisAnalyzer(
                            ActivityCompat.getMainExecutor(applicationContext) ,
                            analyzer
                        )
                    }
                }
                LaunchedEffect(viewModel.result.value){
                    if(viewModel.result.value != null){
                        Toast.makeText(
                            applicationContext ,
                            viewModel.result.value!!.analysis_result + viewModel.result.value!!.filename  ,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                Scaffold {padding ->
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding).onGloballyPositioned { screen ->
                            graphicOverlay.updateGraphicOverlay(
                                width = screen.size.width.toFloat(),
                                height = screen.size.height.toFloat(),
                            )
                        }
                    ){
                        CameraPreview(controller , Modifier.fillMaxSize())
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (bitmapImage.value != null && results.value!=null) {
//                    graphicOverlay.add(CameraImageGraphic(graphicOverlay, bitmapImage.value!!))
                                graphicOverlay.add(PoseGraphic(graphicOverlay, results.value!!))
                                graphicOverlay.onDraw(this)
                                graphicOverlay.clear()
                            }
                        }
                    Row(
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(30.dp),
                        horizontalArrangement = Arrangement.Center
                    ){
                        IconButton(
                            modifier = Modifier.size(40.dp),
                            onClick = {
                                recordVideo(controller,
                                    onRecorded = {file->
                                    viewModel.onUpload(file)
                                }
                                )
                            }
                        ){
                            Icon(
                                imageVector = Icons.Default.AddCircle ,
                                contentDescription = "RECORD VIDEO" ,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        }
                    }
                }
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun recordVideo(controller: LifecycleCameraController , onRecorded :(File) -> Unit){
        count++
        val outputFile = File(filesDir , "my_recording.mp4")
        val uploadFile = File(filesDir , "my_upload_count_${count}.mp4")
        if(recording != null){
            recording?.stop()
            recording = null
            outputFile.copyTo(uploadFile , true)
            onRecorded(uploadFile)
            return
        }
        recording = controller.startRecording(
            FileOutputOptions.Builder(outputFile).build(),
            AudioConfig.create(true),
            ContextCompat.getMainExecutor(applicationContext),
        ){event->
            when(event){
                is VideoRecordEvent.Finalize ->{
                    if(event.hasError()){
                        recording?.close()
                        recording = null
                        Toast.makeText(
                            applicationContext ,
                            "VIDEO CAPTURE FAILED" ,
                            Toast.LENGTH_LONG
                        ).show()
                    }else{
                        Toast.makeText(
                            applicationContext ,
                            "VIDEO CAPTURE SUCCEEDED" ,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

    }
    private fun hasRequiredPermissions(): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )
    }
}

