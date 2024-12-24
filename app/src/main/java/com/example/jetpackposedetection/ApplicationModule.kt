package com.example.jetpackposedetection

import com.example.jetpackposedetection.api.FileRepository
import com.example.jetpackposedetection.constants.FileApiConst
import com.example.mlkit_posedetection_jetpack.api.FileApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun provideFileApi(): FileApi = Retrofit.Builder()
        .baseUrl(FileApiConst.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(FileApi::class.java)

    @Provides
    @Singleton
    fun provideFileRepository(fileApi: FileApi): FileRepository = FileRepository(fileApi)
}