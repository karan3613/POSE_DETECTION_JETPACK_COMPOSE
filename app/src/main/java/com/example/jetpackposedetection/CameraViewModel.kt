package com.example.jetpackposedetection


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackposedetection.api.Analysis
import com.example.jetpackposedetection.api.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repository: FileRepository,
) : ViewModel() {
    private val _result = mutableStateOf<Analysis?>(null)
    val result : State<Analysis?> = _result
    fun onUpload(file  : File) {
        viewModelScope.launch {
            _result.value = repository.uploadVideo(
                file = file
            )
        }
    }
}