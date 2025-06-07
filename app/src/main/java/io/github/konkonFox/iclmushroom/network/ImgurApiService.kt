package io.github.konkonFox.iclmushroom.network

import io.github.konkonFox.iclmushroom.model.ImgurCreditsResponse
import io.github.konkonFox.iclmushroom.model.ImgurDeleteResponse
import io.github.konkonFox.iclmushroom.model.ImgurUploadResponse
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path


private const val BASE_URL = "https://api.imgur.com/3/"

interface ImgurApi {
    @GET("credits")
    suspend fun getCredits(
        @Header("Authorization") authHeader: String,
    ): ImgurCreditsResponse

    @Multipart
    @POST("image")
    suspend fun postImage(
        @Header("Authorization") authHeader: String,
        @Part image: MultipartBody.Part,
        @Part("privacy") privacy: String = "hidden",
    ): ImgurUploadResponse

    @DELETE("image/{hash}")
    suspend fun deleteImage(
        @Header("Authorization") authHeader: String,
        @Path("hash") hash: String,
    ): ImgurDeleteResponse
}

object ImgurApiService {
    val json = Json { ignoreUnknownKeys = true }
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(BASE_URL)
        .build()
    val api: ImgurApi by lazy {
        retrofit.create(ImgurApi::class.java)
    }
}