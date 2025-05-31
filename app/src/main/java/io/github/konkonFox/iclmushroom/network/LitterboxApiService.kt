package io.github.konkonFox.iclmushroom.network

import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


private const val BASE_URL = "https://litterbox.catbox.moe"

interface LitterboxApi {
    @Multipart
    @POST("resources/internals/api.php")
    suspend fun postImage(
        @Part reqtype: MultipartBody.Part,
        @Part time: MultipartBody.Part,
        @Part image: MultipartBody.Part,
    ): String
}

object LitterboxApiService {
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()
    val api: LitterboxApi by lazy {
        retrofit.create(LitterboxApi::class.java)
    }
}