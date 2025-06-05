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

        setContent {
            ICLMushroomTheme {
                IclApp(
                    isMushroom = isMushroom,
                    isShared = isShared,
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