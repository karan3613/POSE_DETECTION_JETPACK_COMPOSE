package com.example.mlkit_posedetection_jetpack.api

import com.example.jetpackposedetection.api.Analysis
import com.example.jetpackposedetection.constants.Resource
import com.google.android.gms.common.api.Api
import okhttp3.MultipartBody

import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

interface FileApi {
    ///////////////////////////////////////////////////////////////////////////
    // UPLOAD
    ///////////////////////////////////////////////////////////////////////////
    @Multipart
    @POST("/uploadVideo")
    suspend fun uploadVideo(
        @Part video : MultipartBody.Part,
    ) : Analysis
}