package io.github.konkonFox.iclmushroom

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.konkonFox.iclmushroom.data.ImgurAccountData
import io.github.konkonFox.iclmushroom.ui.IclApp
import io.github.konkonFox.iclmushroom.ui.theme.ICLMushroomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isMushroom: Boolean = intent.action == "com.adamrocker.android.simeji.ACTION_INTERCEPT"
        val isShared: Boolean = intent.action == Intent.ACTION_SEND
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        }
        // imgur callback
        val isImgurCallback: Boolean = intent.action == Intent.ACTION_VIEW
        val imgurAccessToken: String? = if (isImgurCallback) {
            val accessTokenRegex = Regex("(?<=#access_token=)[^&#?]+")
            val accessToken = accessTokenRegex.find(intent.data.toString())?.value
            accessToken
        } else {
            null
        }
        val imgurAccountName: String? = if (isImgurCallback) {
            val accountNameRegex = Regex("(?<=account_username=)[^&#?]+")
            val accountName = accountNameRegex.find(intent.data.toString())?.value
            accountName
        } else {
            null
        }
        val imgurExpireAt: Long? = if (isImgurCallback) {
            val expiresInRegex = Regex("(?<=expires_in=)[^&#?]+")
            val expiresIn = expiresInRegex.find(intent.data.toString())?.value
            expiresIn?.toLongOrNull()?.times(1000)
                ?.plus(System.currentTimeMillis())
        } else {
            null
        }
        val imgurAccountData = ImgurAccountData(
            accessToken = imgurAccessToken,
            name = imgurAccountName,
            expireAt = imgurExpireAt,
        )

        setContent {
            ICLMushroomTheme {
                IclApp(
                    isMushroom = isMushroom,
                    isShared = isShared,
                    isImgurCallback = isImgurCallback,
                    imgurAccountData = imgurAccountData,
                    sharedUris = listOfNotNull(uri)
                )
            }
        }
    }

    fun returnResultToCaller(resultText: String) {
        val resultIntent = Intent().apply {
            putExtra("replace_key", resultText)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}

@Preview(showBackground = true)
@Composable
fun IclPreview() {
    ICLMushroomTheme {
        IclApp()
    }
}