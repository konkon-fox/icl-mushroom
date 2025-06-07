package io.github.konkonFox.iclmushroom.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.github.konkonFox.iclmushroom.BaseIclViewModel
import io.github.konkonFox.iclmushroom.IclUiState
import io.github.konkonFox.iclmushroom.MockIclViewModel
import io.github.konkonFox.iclmushroom.R
import io.github.konkonFox.iclmushroom.model.ImgurAccountOAuth
import io.github.konkonFox.iclmushroom.ui.components.ListButton
import io.github.konkonFox.iclmushroom.ui.components.TextInputDialog
import io.github.konkonFox.iclmushroom.ui.theme.ICLMushroomTheme


//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: IclUiState,
    viewModel: BaseIclViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isImgurInputDialog by remember { mutableStateOf(false) }
    val isLogin: Boolean =
        uiState.imgurAccessToken.isNotEmpty() && uiState.imgurExpireAt > System.currentTimeMillis()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        HorizontalDivider(thickness = 1.dp)
        if (isLogin) {
            ListButton(
                textRes = R.string.btn_logout_to_imgur,
                onClick = {
                    viewModel.deleteImgurAccountData()
                },
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            ) {
                Text(
                    text = uiState.imgurAccountName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.login_by),
                    fontSize = 12.sp
                )
            }
        } else {
            ListButton(
                textRes = R.string.btn_login_to_imgur,
                onClick = {
                    ImgurAccountOAuth.open(context)
                },
            )
        }
        HorizontalDivider(thickness = 1.dp)
        ListButton(
            textRes = R.string.btn_imgur_settings,
            onClick = {
                isImgurInputDialog = true
            },
        )
        HorizontalDivider(thickness = 1.dp)
    }

    if (isImgurInputDialog) {
        TextInputDialog(
            titleRes = R.string.input_imgur_client_id,
            labelRes = R.string.label_imgur_client_id,
            initialValue = uiState.userClientId,
            onOk = { resultValue ->
                viewModel.updateUserClientId(resultValue)
            },
            closeFun = { isImgurInputDialog = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val mockViewModel = MockIclViewModel()
    val uiState by mockViewModel.uiState.collectAsState()
    ICLMushroomTheme {
        SettingsScreen(
            uiState = uiState,
            viewModel = mockViewModel,
            navController = rememberNavController(),
            modifier = Modifier.fillMaxSize()
        )
    }
}