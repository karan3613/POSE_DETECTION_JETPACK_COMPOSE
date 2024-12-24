package com.example.jetpackposedetection.api

import android.util.Log
import com.example.jetpackposedetection.constants.FileApiConst
import com.example.mlkit_posedetection_jetpack.api.FileApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File

class FileRepository(
    private val api: FileApi,
)  {
    ///////////////////////////////////////////////////////////////////////////
    // UPLOAD
    ///////////////////////////////////////////////////////////////////////////
    private val tempResult = Analysis("NO FILE UPLOADED" ,"NO ANALYTICS")
    suspend fun uploadVideo(file: File): Analysis{
        return withContext(Dispatchers.IO) {
            return@withContext try {
               val result =  api.uploadVideo(
                    MultipartBody.Part.createFormData(
                        FileApiConst.TYPE_VIDEO,
                        file.name,
                        file.asRequestBody()
                    )
                )
                result
            } catch (e: HttpException) {
                Log.d("apiError" , e.message().toString())
                tempResult
            }
        }
    }
}