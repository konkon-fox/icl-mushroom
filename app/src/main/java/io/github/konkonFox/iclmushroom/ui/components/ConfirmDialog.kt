package io.github.konkonFox.iclmushroom.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
fun ConfirmDialog(
    @StringRes titleRes: Int,
    @StringRes bodyRes: Int,
    dynamicBody: String?,
    onOk: () -> Unit,
    closeFun: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = closeFun,
    ) {
        Card(
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
//                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                Button(
                    onClick = {
                        closeFun()
                        onOk()
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.btn_ok)
                    )
                }
                TextButton(
                    onClick = closeFun,
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.btn_back)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfirmDialogPreview() {
    ConfirmDialog(
        titleRes = R.string.dialog_title_upload_error,
        bodyRes = R.string.dialog_body_too_match,
        dynamicBody = null,
        onOk = {},
        closeFun = {}
    )
}