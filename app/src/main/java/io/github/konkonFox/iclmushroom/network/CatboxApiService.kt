package io.github.konkonFox.iclmushroom.network

import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


private const val BASE_URL = "https://catbox.moe"

interface CatboxApi {
    @Multipart
    @POST("user/api.php")
    suspend fun postImage(
        @Part reqtype: MultipartBody.Part,
        @Part image: MultipartBody.Part,
    ): String
}

object CatboxApiService {
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()
    val api: CatboxApi by lazy {
        retrofit.create(CatboxApi::class.java)
    }
}