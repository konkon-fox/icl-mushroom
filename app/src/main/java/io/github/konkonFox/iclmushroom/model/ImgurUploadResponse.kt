package io.github.konkonFox.iclmushroom.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseImgurData(
    val id: String,
    val link: String,
    @SerialName(value = "deletehash")
    val deleteHash: String,
)

@Serializable
data class ImgurUploadResponse(
    val status: Int,
    val success: Boolean,
    val data: ResponseImgurData,
)