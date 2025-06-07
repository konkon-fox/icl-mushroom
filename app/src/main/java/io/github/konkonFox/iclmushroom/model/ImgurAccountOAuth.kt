package io.github.konkonFox.iclmushroom.model

import android.content.Context
import androidx.core.net.toUri
import io.github.konkonFox.iclmushroom.data.ImgurConfig
import io.github.konkonFox.iclmushroom.ui.components.openCustomTab

object ImgurAccountOAuth {
    val clientId = ImgurConfig.getClientId()
    val url = "https://api.imgur.com/oauth2/authorize".toUri()
        .buildUpon()
        .appendQueryParameter("client_id", clientId)
        .appendQueryParameter("response_type", "token")
        .appendQueryParameter("state", "state")
        .build().toString()

    fun open(context: Context) {
        openCustomTab(context, url)
    }
}