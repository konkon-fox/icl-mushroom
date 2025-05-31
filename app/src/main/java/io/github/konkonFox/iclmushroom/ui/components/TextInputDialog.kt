package io.github.konkonFox.iclmushroom.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import io.github.konkonFox.iclmushroom.MockIclViewModel
import io.github.konkonFox.iclmushroom.R
import io.github.konkonFox.iclmushroom.ui.screen.HomeScreen
import io.github.konkonFox.iclmushroom.ui.theme.ICLMushroomTheme


@Composable
fun TextInputDialog(
    @StringRes
    titleRes: Int,
    @StringRes
    labelRes: Int,
    initialValue:String,
    onOk: (resultValue: String) -> Unit,
    closeFun: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var inputValue by remember { mutableStateOf(initialValue) }
    Dialog(
        onDismissRequest = closeFun,
    ) {
        Card(modifier = modifier) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp
            )) {
                Text(
                    text = stringResource(titleRes)
                )
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    label = {
                        Text(
                            text = stringResource(labelRes)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        onOk(inputValue)
                        closeFun()
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
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
fun TextInputDialogPreview() {
    ICLMushroomTheme {
        TextInputDialog(
            titleRes = R.string.input_imgur_client_id,
            labelRes = R.string.label_imgur_client_id,
            initialValue = "",
            onOk = { resultValue ->

            },
            closeFun = {  }
        )
    }
}