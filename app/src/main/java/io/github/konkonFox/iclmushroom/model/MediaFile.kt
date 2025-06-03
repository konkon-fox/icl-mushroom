package io.github.konkonFox.iclmushroom.model

import java.io.File

data class MediaFile(
    val isVideo: Boolean,
    val file: File,
)