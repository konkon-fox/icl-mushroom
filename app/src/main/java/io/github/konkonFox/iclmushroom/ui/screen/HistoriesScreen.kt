package io.github.konkonFox.iclmushroom.ui.screen

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import io.github.konkonFox.iclmushroom.BaseIclViewModel
import io.github.konkonFox.iclmushroom.DialogOptions
import io.github.konkonFox.iclmushroom.IclUiState
import io.github.konkonFox.iclmushroom.LocalClickOption
import io.github.konkonFox.iclmushroom.MainActivity
import io.github.konkonFox.iclmushroom.MockIclViewModel
import io.github.konkonFox.iclmushroom.R
import io.github.konkonFox.iclmushroom.UploaderName
import io.github.konkonFox.iclmushroom.data.LocalItem
import io.github.konkonFox.iclmushroom.ui.components.ConfirmDialog
import io.github.konkonFox.iclmushroom.ui.components.NoticeDialog
import io.github.konkonFox.iclmushroom.ui.components.NowLoading
import io.github.konkonFox.iclmushroom.ui.theme.ICLMushroomTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun formatTimestampCompat(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}


private data class Radio(val label: String, val value: LocalClickOption)

@Composable
fun DeleteDialog(
    deleteFromServer: () -> Unit,
    deleteFromHistories: () -> Unit,
    isDeletableFromServer: Boolean,
    closeFun: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = {}) {
        Card(modifier = modifier) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                if (isDeletableFromServer) {
                    Button(
                        onClick = deleteFromServer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_delete_from_server)
                        )
                    }
                }
                Button(
                    onClick = deleteFromHistories,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_delete_from_histories)
                    )
                }
                TextButton(
                    onClick = closeFun,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_back)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun Item(
    uiState: IclUiState,
    viewModel: BaseIclViewModel,
    item: LocalItem,
    clickOption: LocalClickOption,
    isMushroom: Boolean,
    nowTime: Long,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    var isDialogOpen by remember { mutableStateOf(false) }
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()


    fun clickHandle() {
        when (clickOption) {
            LocalClickOption.Insert -> {
                if (isMushroom && activity != null) {
                    val result = "\n${item.link}\n"
                    activity.returnResultToCaller(result)
                }
            }

            LocalClickOption.Copy -> {
                coroutineScope.launch {
                    clipboard.setClipEntry(
                        ClipData.newPlainText(item.link, item.link).toClipEntry()
                    )
                }
            }
        }
    }

    fun longClickHandle() {
        isDialogOpen = true
    }

    val imageSrc = if (item.uploader == UploaderName.Imgur.name) {
        val unixParam: String = item.link + if (item.isDeleted) {
            "?$nowTime"
        } else {
            ""
        }
        item.link.replace(Regex("\\.[^.]+?$"), "m.jpeg") + unixParam
    } else {
        item.link
    }

    //
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.combinedClickable(
            onClick = { clickHandle() },
            onLongClick = { longClickHandle() },
            indication = LocalIndication.current,
            interactionSource = remember { MutableInteractionSource() }
        )
    ) {
        if (item.uploader != UploaderName.Imgur.name && item.isVideo) {
            Image(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                modifier = Modifier.size(60.dp)
            )
        } else {
            AsyncImage(
                model = imageSrc,
                contentDescription = null,
                placeholder = rememberVectorPainter(Icons.Default.Downloading),
                error = rememberVectorPainter(Icons.Default.BrokenImage),
                modifier = Modifier.size(60.dp)
            )
        }
        Column(
            modifier = Modifier
                .padding(
                    start = 8.dp,
                    top = 4.dp,
                    end = 4.dp,
                    bottom = 4.dp
                )
                .weight(1f)
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatTimestampCompat(item.createdAt),
                    fontSize = 14.sp
                )
                if (item.deleteAt != null) {
                    Text(
                        text = "-",
                        fontSize = 14.sp
                    )
                    Text(
                        text = formatTimestampCompat(item.deleteAt),
                        fontSize = 14.sp
                    )
                }
            }
            Text(
                text = item.link,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp
            )
            if (item.useImgurAccount) {
                Text(
                    text = stringResource(R.string.authed),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Right,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    if (!uiState.nowLoadingOption.isOpen && isDialogOpen) {
        DeleteDialog(
            deleteFromServer = {
                viewModel.deleteImgurItem(item)
                isDialogOpen = false
            },
            deleteFromHistories = {
                viewModel.deleteLocalItem(item)
                isDialogOpen = false
            },
            isDeletableFromServer = item.uploader == UploaderName.Imgur.name && item.deleteHash != null && item.isDeleted == false,
            closeFun = { isDialogOpen = false },
        )
    }
}

@Composable
fun DropdownMenu(viewModel: BaseIclViewModel) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(8.dp)
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.btn_delete_imgur_from_histories)) },
                onClick = {
                    viewModel.openConfirmDialog(
                        DialogOptions(
                            isOpen = true,
                            title = R.string.dialog_title_delete_confirm,
                            body = R.string.dialog_body_delete_imgur_from_histories,
                            dynamicBody = null,
                            onOk = {
                                expanded = false
                                viewModel.deleteDeletedImgurItems()
                            },
                            closeFun = {
                                expanded = false
                                viewModel.closeConfirmDialog()
                            }
                        )
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.btn_delete_litterbox_from_histories)) },
                onClick = {
                    viewModel.openConfirmDialog(
                        DialogOptions(
                            isOpen = true,
                            title = R.string.dialog_title_delete_confirm,
                            body = R.string.dialog_body_delete_litterbox_from_histories,
                            dynamicBody = null,
                            onOk = {
                                expanded = false
                                viewModel.deleteExpiredLitterboxItems()
                            },
                            closeFun = {
                                expanded = false
                                viewModel.closeConfirmDialog()
                            }
                        )
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.btn_delete_all_from_histories)) },
                onClick = {
                    viewModel.openConfirmDialog(
                        DialogOptions(
                            isOpen = true,
                            title = R.string.dialog_title_delete_confirm,
                            body = R.string.dialog_body_delete_all_from_histories,
                            dynamicBody = null,
                            onOk = {
                                expanded = false
                                viewModel.deleteAllLocalItems()
                            },
                            closeFun = {
                                expanded = false
                                viewModel.closeConfirmDialog()
                            }
                        )
                    )
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoriesScreen(
    uiState: IclUiState,
    viewModel: BaseIclViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val clickRadios = listOf(
        Radio(stringResource(R.string.radio_insert), LocalClickOption.Insert),
        Radio(stringResource(R.string.radio_copy), LocalClickOption.Copy),
    )
    val nowTime: Long = System.currentTimeMillis()

//    HorizontalDivider(thickness = 1.dp)
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        text = stringResource(R.string.histories_screen),
                    )
                },
                actions = {
                    DropdownMenu(viewModel)
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_local_click_option),
                )
                for (clickRadio in clickRadios) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .selectable(
                                selected = uiState.localClickOption == clickRadio.value,
                                onClick = {
                                    viewModel.updateLocalClickOption(clickRadio.value)
                                },
                                role = Role.RadioButton
                            )
                    ) {
                        RadioButton(
                            selected = uiState.localClickOption == clickRadio.value,
                            onClick = {
                                viewModel.updateLocalClickOption(clickRadio.value)
                            },
                        )
                        Text(text = clickRadio.label)
                    }
                }

            }
            HorizontalDivider(thickness = 1.dp)
            if (uiState.localItems.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_items),
                    modifier = Modifier.padding(16.dp)
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(uiState.localItems) { item ->
                    Item(
                        viewModel = viewModel,
                        item = item,
                        clickOption = uiState.localClickOption,
                        isMushroom = uiState.isMushroom,
                        uiState = uiState,
                        nowTime = nowTime,
                    )
                    HorizontalDivider(thickness = 1.dp)
                }
            }
        }

        if (uiState.nowLoadingOption.isOpen) {
            NowLoading(
                titleRes = uiState.nowLoadingOption.title
            )
        } else if (uiState.confirmDialogOptions.isOpen) {
            ConfirmDialog(
                titleRes = uiState.confirmDialogOptions.title,
                bodyRes = uiState.confirmDialogOptions.body,
                dynamicBody = uiState.confirmDialogOptions.dynamicBody,
                onOk = uiState.confirmDialogOptions.onOk,
                closeFun = uiState.confirmDialogOptions.closeFun
            )
        } else if (uiState.dialogOptions.isOpen) {
            NoticeDialog(
                titleRes = uiState.dialogOptions.title,
                bodyRes = uiState.dialogOptions.body,
                dynamicBody = uiState.dialogOptions.dynamicBody,
                onOk = {},
                closeFun = { viewModel.closeDialog() }
            )

        }
    }
}


@Preview(showBackground = true)
@Composable
fun HistoriesScreenPreview() {
    val mockViewModel = MockIclViewModel()
    val uiState by mockViewModel.uiState.collectAsState()
    ICLMushroomTheme {
        HistoriesScreen(
            uiState = uiState,
            viewModel = mockViewModel,
            navController = rememberNavController(),
            modifier = Modifier.fillMaxSize()
        )
    }
}