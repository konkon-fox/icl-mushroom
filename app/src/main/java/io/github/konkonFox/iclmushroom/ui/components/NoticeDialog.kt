package io.github.konkonFox.iclmushroom.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.konkonFox.iclmushroom.R

@Composable
fun NoticeDialog(
    @StringRes titleRes: Int,
    @StringRes bodyRes: Int,
    dynamicBody: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = {},
    ) {
        Card(
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
//                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = stringResource(titleRes),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Text(
                    text =
                        if (dynamicBody == null) {
                            stringResource(bodyRes)
                        } else {
                            stringResource(bodyRes, dynamicBody)
                        },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                TextButton(onClick = onClick, modifier = Modifier.height(48.dp)) {
                    Text(
                        text = stringResource(R.string.btn_ok)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoticeDialogPreview() {
    NoticeDialog(
        titleRes = R.string.dialog_title_upload_error,
        bodyRes = R.string.dialog_body_too_match,
        dynamicBody = null,
        onClick = {}
    )
}