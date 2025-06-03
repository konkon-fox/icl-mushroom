package io.github.konkonFox.iclmushroom.ui.screen

import android.app.Activity
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.github.konkonFox.iclmushroom.BaseIclViewModel
import io.github.konkonFox.iclmushroom.BuildConfig
import io.github.konkonFox.iclmushroom.DialogOptions
import io.github.konkonFox.iclmushroom.IclUiState
import io.github.konkonFox.iclmushroom.MockIclViewModel
import io.github.konkonFox.iclmushroom.R
import io.github.konkonFox.iclmushroom.ui.IclScreen
import io.github.konkonFox.iclmushroom.ui.components.ListButton
import io.github.konkonFox.iclmushroom.ui.components.NoticeDialog
import io.github.konkonFox.iclmushroom.ui.components.TextInputDialog
import io.github.konkonFox.iclmushroom.ui.theme.ICLMushroomTheme


@Composable
fun UploadButton(viewModel: BaseIclViewModel, navController: NavController) {
    val context = LocalContext.current

    // 従来の OpenMultipleDocuments ランチャー（API 32 以下向け）
    val legacyLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        viewModel.onImagesSelected(context, uris, navController)
    }

    // Photo Picker ランチャー（API 33 以上向け）
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        viewModel.onImagesSelected(context, uris, navController)
    }

    val handleClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+) で Photo Picker を使用
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
            )
        } else {
            // Android 12 以下では従来のドキュメントピッカーを使用
            legacyLauncher.launch(arrayOf("image/*", "video/*"))
        }
    }

    ListButton(
        textRes = R.string.btn_upload,
        onClick = handleClick,
    )
}


//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: IclUiState,
    viewModel: BaseIclViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var isImgurInputDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        HorizontalDivider(thickness = 1.dp)
        UploadButton(viewModel, navController)
        HorizontalDivider(thickness = 1.dp)
        ListButton(
            textRes = R.string.btn_upload_history,
            onClick = {
                navController.navigate(IclScreen.Histories.name)
            },
        )
        HorizontalDivider(thickness = 1.dp)
        ListButton(
            textRes = R.string.btn_imgur_settings,
            onClick = {
                isImgurInputDialog = true
            },
        )
        HorizontalDivider(thickness = 1.dp)
        ListButton(
            textRes = R.string.btn_info,
            onClick = {
                viewModel.openDialog(
                    DialogOptions(
                        isOpen = true,
                        title = R.string.dialog_title_version_info,
                        body = R.string.dialog_body_version_info,
                        dynamicBody = BuildConfig.VERSION_NAME.toString(),
                        onOk = {},
                        closeFun = {},
                    )
                )
            },
        )
        HorizontalDivider(thickness = 1.dp)
    }
    if (uiState.dialogOptions.isOpen) {
        NoticeDialog(
            titleRes = uiState.dialogOptions.title,
            bodyRes = uiState.dialogOptions.body,
            dynamicBody = uiState.dialogOptions.dynamicBody,
            onClick = { viewModel.closeDialog() }
        )
    } else if (isImgurInputDialog) {
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
fun HomeScreenPreview() {
    val mockViewModel = MockIclViewModel()
    val uiState by mockViewModel.uiState.collectAsState()
    ICLMushroomTheme {
        HomeScreen(
            uiState = uiState,
            viewModel = mockViewModel,
            navController = rememberNavController(),
            modifier = Modifier.fillMaxSize()
        )
    }
}