package io.github.konkonFox.iclmushroom.ui.components

import android.content.Context
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import io.github.konkonFox.iclmushroom.R
import io.github.konkonFox.iclmushroom.ui.theme.ICLMushroomTheme
import androidx.core.net.toUri

@Composable
fun LinkText(
    @StringRes
    textRes: Int,
    @StringRes
    urlRes: Int,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val text = stringResource(textRes)
    val annotatedText = buildAnnotatedString {
        append(text)
        addStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            ),
            start = 0,
            end = text.length
        )
        addStringAnnotation(
            tag = "URL",
            annotation = stringResource(urlRes),
            start = 0,
            end = text.length
        )
    }
    Row{
        Icon(
            Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "open in new"
            , tint = MaterialTheme.colorScheme.primary
        )
        @Suppress("DEPRECATION")
        ClickableText(
            text = annotatedText,
            style = MaterialTheme.typography.bodyLarge,
            onClick = { offset ->
                annotatedText.getStringAnnotations("URL", offset, offset)
                    .firstOrNull()?.let { stringAnnotation ->
                        openCustomTab(context, stringAnnotation.item)
                    }
            },
            modifier = modifier
        )
    }
}

fun openCustomTab(context: Context, url: String) {
    val intent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build()
    intent.launchUrl(context, url.toUri())
}


@Preview(showBackground = true)
@Composable
fun LinkTextPreview() {
    ICLMushroomTheme {
        LinkText(
            textRes = R.string.tos,
            urlRes = R.string.url_imgur_tos
        )
    }
}