package io.github.konkonFox.iclmushroom

import android.app.Activity
import android.content.Intent
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

        setContent {
            ICLMushroomTheme {
                IclApp(isMushroom)
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