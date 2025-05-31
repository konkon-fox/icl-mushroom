package io.github.konkonFox.iclmushroom.model

import kotlinx.serialization.Serializable

@Serializable
data class ImgurDeleteResponse(
    val status: Int,
    val success: Boolean,
)