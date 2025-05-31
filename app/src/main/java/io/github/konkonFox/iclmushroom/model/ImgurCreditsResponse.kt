package io.github.konkonFox.iclmushroom.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseCreditsData(
    @SerialName(value = "UserRemaining")
    val userRemaining: Int,
    @SerialName(value = "ClientRemaining")
    val clientRemaining: Int,
)

@Serializable
data class ImgurCreditsResponse(
    val status: Int,
    val success: Boolean,
    val data: ResponseCreditsData,
)