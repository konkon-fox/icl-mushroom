package io.github.konkonFox.iclmushroom.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.github.konkonFox.iclmushroom.BaseIclViewModel
import io.github.konkonFox.iclmushroom.IclUiState
import io.github.konkonFox.iclmushroom.LitterboxHourOption
import io.github.konkonFox.iclmushroom.MainActivity
import io.github.konkonFox.iclmushroom.MockIclViewModel
import io.github.konkonFox.iclmushroom.R
import io.github.konkonFox.iclmushroom.UploaderName
import io.github.konkonFox.iclmushroom.UploaderName.Catbox
import io.github.konkonFox.iclmushroom.UploaderName.Imgur
import io.github.konkonFox.iclmushroom.UploaderName.Litterbox
import io.github.konkonFox.iclmushroom.ui.components.LinkText
import io.github.konkonFox.iclmushroom.ui.components.NoticeDialog
import io.github.konkonFox.iclmushroom.ui.components.NowLoading
import io.github.konkonFox.iclmushroom.ui.theme.ICLMushroomTheme

private data class ReduceOption(val label: String, val value: Int?)

@Composable
fun UploadScreen(
    uiState: IclUiState,
    viewModel: BaseIclViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val uploaderList: List<UploaderName> = UploaderName.entries
    //
    val reduceOptions = listOf(
        ReduceOption("しない", null),
        ReduceOption("3840", 3840),
        ReduceOption("1920", 1920),
        ReduceOption("1024", 1024),
    )
    val reduceOptionSaver: Saver<ReduceOption, List<Any?>> = Saver(
        save = { listOf(it.label, it.value) },
        restore = {
            val label = it[0] as String
            val value = it[1] as Int?
            ReduceOption(label, value)
        }
    )
    val (selectedReduceOption, onReduceOptionSelected) = rememberSaveable(stateSaver = reduceOptionSaver) {
        mutableStateOf(reduceOptions[0])
    }
    //
    val litterboxHourOptions = listOf(
        LitterboxHourOption("1h", 1),
        LitterboxHourOption("12h", 12),
        LitterboxHourOption("24h", 24),
        LitterboxHourOption("72h", 72)
    )
    val litterboxOptionSaver: Saver<LitterboxHourOption, List<Any?>> = Saver(
        save = { listOf(it.label, it.hour) },
        restore = {
            val label = it[0] as String
            val hour = it[1] as Int
            LitterboxHourOption(label, hour)
        }
    )
    val (selectedLitterboxOption, onLitterboxOptionSelected) = rememberSaveable(stateSaver = litterboxOptionSaver) {
        mutableStateOf(litterboxHourOptions[0])
    }
    val context = LocalContext.current
    val tomLinkRes = when (uiState.selectedUploader) {
        Imgur -> R.string.url_imgur_tos
        Catbox -> R.string.url_catbox_tos
        Litterbox -> R.string.url_litterbox_tos
    }
    //

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_uploader),
                )
                for (uploaderName in uploaderList) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .selectable(
                                selected = uiState.selectedUploader == uploaderName,
                                onClick = {
                                    viewModel.updateSelectedUploader(uploaderName)
                                },
                                role = Role.RadioButton
                            )
                    ) {
                        RadioButton(
                            selected = uiState.selectedUploader == uploaderName,
                            onClick = {
                                viewModel.updateSelectedUploader(uploaderName)
                            },
                        )
                        Text(text = uploaderName.name)
                    }
                }
            }
            HorizontalDivider(thickness = 1.dp)

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.reduce_image_size)
                )
                for (reduceOption in reduceOptions) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .selectable(
                                selected = reduceOption == selectedReduceOption,
                                onClick = {
                                    onReduceOptionSelected(reduceOption)
                                },
                                role = Role.RadioButton
                            )
                    ) {
                        RadioButton(
                            selected = reduceOption == selectedReduceOption,
                            onClick = {
                                onReduceOptionSelected(reduceOption)
                            },
                        )
                        Text(
                            text = reduceOption.label
                        )
                    }
                }
            }
            HorizontalDivider(thickness = 1.dp)


            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(16.dp)
                    .selectable(
                        selected = uiState.isDeleteExif,
                        onClick = { viewModel.updateIsDeleteExif(!uiState.isDeleteExif) },
                        role = Role.Checkbox
                    ),

                ) {
                Checkbox(
                    checked = uiState.isDeleteExif,
                    onCheckedChange = { it ->
                        viewModel.updateIsDeleteExif(it)
                    }
                )
                Text(
                    text = stringResource(R.string.checkbox_delete_exif)
                )
            }
            HorizontalDivider(thickness = 1.dp)

            if (uiState.selectedUploader.name == Litterbox.name) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.expires)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (option in litterboxHourOptions) {
                            FilterChip(
                                selected = option == selectedLitterboxOption,
                                onClick = { onLitterboxOptionSelected(option) },
                                label = {
                                    Text(
                                        text = option.label
                                    )
                                }
                            )
                        }
                    }
                }
                HorizontalDivider(thickness = 1.dp)
            }
        }
        HorizontalDivider(thickness = 1.dp)
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp).fillMaxWidth()
            ) {
                LinkText(
                    textRes = R.string.tos,
                    urlRes = tomLinkRes
                )
                Text(
                    text = stringResource(R.string.please_comply_with)
                )
            }
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                TextButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxHeight(),
                    shape = RectangleShape
                ) {
                    Text(
                        text = stringResource(R.string.btn_cancel)
                    )
                }
                TextButton(
                    onClick = {
                        viewModel.uploadImages(
                            context = context,
                            navController = navController,
                            reduceSize = selectedReduceOption.value,
                            expiresHour = selectedLitterboxOption.hour,
                            onResult = { list ->
                                if (list.isEmpty()) return@uploadImages
                                val activity = context as? MainActivity
                                if (uiState.isMushroom && activity != null) {
                                    val result = "\n" + list.joinToString("\n") + "\n"
                                    activity.returnResultToCaller(result)
                                }
                            }
                        )
                    },
                    modifier = Modifier.height(48.dp),
                    shape = RectangleShape
                ) {
                    Text(
                        text = stringResource(R.string.btn_confirm_upload)
                    )
                }
            }
        }
    }
    if (uiState.nowLoadingOption.isOpen) {
        NowLoading(
            titleRes = uiState.nowLoadingOption.title
        )
    } else if (uiState.dialogOptions.isOpen) {
        NoticeDialog(
            titleRes = uiState.dialogOptions.title,
            bodyRes = uiState.dialogOptions.body,
            dynamicBody = uiState.dialogOptions.dynamicBody,
            onClick = { viewModel.closeDialog() }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun UploadScreenPreview() {
    val mockViewModel = MockIclViewModel()
    val uiState by mockViewModel.uiState.collectAsState()
    ICLMushroomTheme {
        UploadScreen(
            uiState = uiState,
            viewModel = mockViewModel,
            navController = rememberNavController(),
            modifier = Modifier.fillMaxSize()
        )
    }
}