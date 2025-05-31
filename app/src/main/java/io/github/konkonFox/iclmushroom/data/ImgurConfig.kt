package io.github.konkonFox.iclmushroom.data

import io.github.konkonFox.iclmushroom.BuildConfig

object ImgurConfig {
    private const val ENCODED = BuildConfig.ENCODED_IMGUR_CLIENT_ID
    fun getClientId(): String =
        String(android.util.Base64.decode(ENCODED, android.util.Base64.DEFAULT))
}