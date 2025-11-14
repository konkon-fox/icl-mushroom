package io.github.konkonFox.iclmushroom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Precision
import io.github.konkonFox.iclmushroom.data.IclRepository
import io.github.konkonFox.iclmushroom.data.ImgurAccountData
import io.github.konkonFox.iclmushroom.data.LocalItem
import io.github.konkonFox.iclmushroom.model.MediaFile
import io.github.konkonFox.iclmushroom.ui.IclScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

enum class UploaderName {
    Imgur,
    Catbox,
    Litterbox
}

data class LitterboxHourOption(val label: String, val hour: Int)

enum class LocalClickOption {
    Insert,
    Copy
}

enum class ImageLauncherType {
    PhotoPicker,
    Legacy
}

data class DialogOptions(
    val isOpen: Boolean = false,
    @StringRes val title: Int = R.string.dummy,
    @StringRes val body: Int = R.string.dummy,
    val dynamicBody: String? = null,
    val onOk: () -> Unit = {},
    val closeFun: () -> Unit = {},
)

data class NowLoadingOptions(
    val isOpen: Boolean = false,
    @StringRes val title: Int = R.string.dummy,
)

data class IclUiState(
    val selectedUploader: UploaderName = UploaderName.Imgur,
    val selectedFiles: List<Uri> = emptyList(),
    val userClientId: String = "",
    val isDeleteExif: Boolean = false,
    val nowLoadingOption: NowLoadingOptions = NowLoadingOptions(),
    val dialogOptions: DialogOptions = DialogOptions(),
    val confirmDialogOptions: DialogOptions = DialogOptions(),
    val localItems: List<LocalItem> = emptyList(),
    val localClickOption: LocalClickOption = LocalClickOption.Insert,
    val isMushroom: Boolean = false,
    val targetLocalItem: LocalItem? = null,
    val isShared: Boolean = false,
    val isCopyUrlAfterUpload: Boolean = false,
    val imgurAccessToken: String = "",
    val imgurAccountName: String = "",
    val imgurExpireAt: Long = 0,
    val imageLauncherType: ImageLauncherType = ImageLauncherType.PhotoPicker,
    val useImgurAccount: Boolean = false,
    val catboxUserHash: String = "",
)

interface BaseIclViewModel {
    val uiState: StateFlow<IclUiState>
    fun updateSelectedUploader(uploader: UploaderName)
    fun updateUserClientId(clientId: String)
    fun updateLocalClickOption(option: LocalClickOption)
    fun updateIsDeleteExif(checked: Boolean)
    fun setIsMushroom(boolean: Boolean)
    fun onImagesSelected(context: Context, uris: List<Uri>, navController: NavController)
    fun openDialog(dialogOptions: DialogOptions)
    fun closeDialog()
    fun openConfirmDialog(dialogOptions: DialogOptions)
    fun closeConfirmDialog()
    fun uploadImages(
        context: Context,
        navController: NavController,
        reduceSize: Int?,
        expiresHour: Int,
        onResult: (List<String>) -> Unit,
    )

    fun deleteLocalItem(item: LocalItem)
    fun deleteServerItem(item: LocalItem)
    fun deleteDeletedItems()
    fun deleteExpiredLitterboxItems()
    fun deleteAllLocalItems()
    fun updateIsCopyUrlAfterUpload(checked: Boolean)
    fun setIsShared(boolean: Boolean)
    fun updateImgurAccountData(imgurAccountData: ImgurAccountData)
    fun deleteImgurAccountData()
    fun updateUseImgurAccount(boolean: Boolean)

    fun updateImageLauncherType(launcherType: ImageLauncherType)
    fun updateCatboxUserHash(hash: String)
}

class IclViewModel(
    private val iclRepository: IclRepository,
) : ViewModel(), BaseIclViewModel {

    // viewModel内部
    private val _uiState = MutableStateFlow(IclUiState())

    // viewModel外部
    override val uiState: StateFlow<IclUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                iclRepository.selectedUploader,
                iclRepository.userClientId,
                iclRepository.localClickOption,
                iclRepository.isDeleteExif,
                iclRepository.isCopyUrlAfterUpload,
            ) { uploader, clientId, option, deleteExif, copyUrl ->
                _uiState.update {
                    it.copy(
                        selectedUploader = UploaderName.valueOf(uploader),
                        userClientId = clientId,
                        localClickOption = LocalClickOption.valueOf(option),
                        isDeleteExif = deleteExif,
                        isCopyUrlAfterUpload = copyUrl
                    )
                }
            }.collect()
        }
        viewModelScope.launch {
            combine(
                iclRepository.imgurAccessToken,
                iclRepository.imgurAccountName,
                iclRepository.imgurExpireAt,
                iclRepository.useImgurAccount,
            ) { token, name, expireAt, checked ->
                _uiState.update {
                    it.copy(
                        imgurAccessToken = token,
                        imgurAccountName = name,
                        imgurExpireAt = expireAt,
                        useImgurAccount = checked
                    )
                }
            }.collect()
        }
        viewModelScope.launch {
            iclRepository.getAllLocalItems()
                .collect { items ->
                    _uiState.update { it.copy(localItems = items) }
                }
        }
        viewModelScope.launch {
            iclRepository.imageLauncherType.collect { launcherType ->
                _uiState.update {
                    it.copy(imageLauncherType = launcherType)
                }
            }
        }
        viewModelScope.launch {
            iclRepository.catboxUserHash.collect { hash ->
                _uiState.update {
                    it.copy(catboxUserHash = hash)
                }
            }
        }
    }


    // アップローダー変更
    override fun updateSelectedUploader(uploader: UploaderName) {
        viewModelScope.launch {
            iclRepository.updateSelectedUploader(uploader)
        }
    }

    // ユーザーClient id変更
    override fun updateUserClientId(clientId: String) {
        viewModelScope.launch {
            iclRepository.updateUserClientId(clientId)
        }
    }

    // ローカル画像クリック動作変更
    override fun updateLocalClickOption(option: LocalClickOption) {
        viewModelScope.launch {
            iclRepository.updateLocalClickOption(option)
        }
    }

    // exif削除判定　変更
    override fun updateIsDeleteExif(checked: Boolean) {
        viewModelScope.launch {
            iclRepository.updateIsDeleteExif(checked)
        }
    }

    // アップロード後にURLコピー判定　変更
    override fun updateIsCopyUrlAfterUpload(checked: Boolean) {
        viewModelScope.launch {
            iclRepository.updateIsCopyUrlAfterUpload(checked)
        }
    }

    // マッシュルーム判別
    override fun setIsMushroom(boolean: Boolean) {
        _uiState.update {
            it.copy(
                isMushroom = boolean
            )
        }
    }

    // シェアボタン起動判別
    override fun setIsShared(boolean: Boolean) {
        _uiState.update {
            it.copy(
                isShared = boolean
            )
        }
    }

    // ダイアログ開く
    override fun openDialog(dialogOptions: DialogOptions) {
        _uiState.update {
            it.copy(
                dialogOptions = dialogOptions
            )
        }
    }

    // ダイアログ開く
    override fun openConfirmDialog(dialogOptions: DialogOptions) {
        _uiState.update {
            it.copy(
                confirmDialogOptions = dialogOptions
            )
        }
    }

    // ダイアログ閉じる
    override fun closeDialog() {
        _uiState.update {
            it.copy(
                dialogOptions = DialogOptions()
            )
        }
    }

    // ダイアログ閉じる
    override fun closeConfirmDialog() {
        _uiState.update {
            it.copy(
                confirmDialogOptions = DialogOptions()
            )
        }
    }

    // 画像選択
    override fun onImagesSelected(context: Context, uris: List<Uri>, navController: NavController) {
        if (uris.size > 5) {
            openDialog(
                DialogOptions(
                    isOpen = true,
                    title = R.string.dialog_title_upload_error,
                    body = R.string.dialog_body_too_match,
                    dynamicBody = null
                )
            )
        } else if (uris.isNotEmpty()) {
            _uiState.update { it.copy(selectedFiles = uris) }
            navController.navigate(IclScreen.Upload.name)
        }
    }

    // 画像アップロード
    override fun uploadImages(
        context: Context,
        navController: NavController,
        reduceSize: Int?,
        expiresHour: Int,
        onResult: (List<String>) -> Unit,
    ) {
        val urls = mutableListOf<String>()
        viewModelScope.launch {
            val uris: List<Uri> = _uiState.value.selectedFiles
            val (imageUris, videoUris) = uris.partition { uri ->
                val mineType = context.contentResolver.getType(uri) ?: ""
                mineType.startsWith("image/")
            }

            val resizedImageFiles: List<File> = if (reduceSize == null) {
                imageUris.mapNotNull { uri -> uriToFile(context = context, uri = uri) }
            } else {
                resizeImages(
                    context = context,
                    uris = uris,
                    maxSize = reduceSize,
                )
            }
            val finalImageFiles: List<File> = if (_uiState.value.isDeleteExif) {
                resizedImageFiles.map { file ->
                    val exif = ExifInterface(file.absolutePath)
                    val exifTags = listOf(
                        ExifInterface.TAG_MAKE,
                        ExifInterface.TAG_MODEL,
                        ExifInterface.TAG_DATETIME,
                        ExifInterface.TAG_DATETIME_ORIGINAL,
                        ExifInterface.TAG_DATETIME_DIGITIZED,
                        ExifInterface.TAG_GPS_LATITUDE,
                        ExifInterface.TAG_GPS_LONGITUDE,
                        ExifInterface.TAG_GPS_ALTITUDE,
                        ExifInterface.TAG_GPS_PROCESSING_METHOD,
                        ExifInterface.TAG_USER_COMMENT,
                        ExifInterface.TAG_SOFTWARE,
                        ExifInterface.TAG_ARTIST,
                        ExifInterface.TAG_COPYRIGHT,
                        ExifInterface.TAG_IMAGE_DESCRIPTION
                    )
                    for (tag in exifTags) {
                        exif.setAttribute(tag, null)
                    }
                    exif.saveAttributes()
                    file
                }
            } else {
                resizedImageFiles
            }

            val videoFiles: List<File> = videoUris.mapNotNull { uri ->
                uriToFile(context, uri)
            }
            val allMediaFiles: List<MediaFile> =
                finalImageFiles.map({
                    MediaFile(
                        isVideo = false,
                        file = it,
                    )
                }) +
                        videoFiles.map({
                            MediaFile(
                                isVideo = true,
                                file = it,
                            )
                        })

            _uiState.update {
                it.copy(
                    nowLoadingOption = NowLoadingOptions(
                        isOpen = true,
                        title = R.string.now_loading_upload
                    )
                )
            }
            if (_uiState.value.selectedUploader == UploaderName.Imgur && !_uiState.value.useImgurAccount) {
                val isOk: Boolean = iclRepository.checkImgurCredits()
                if (!isOk) {
                    _uiState.update {
                        it.copy(
                            dialogOptions = DialogOptions(
                                isOpen = true,
                                title = R.string.dialog_title_upload_error,
                                body = R.string.dialog_body_imgur_api_error,
                            ),
                            nowLoadingOption = NowLoadingOptions()
                        )
                    }
                    onResult(emptyList())
                    return@launch
                }
            }
            val result: Result<List<LocalItem>> =
                iclRepository.uploadImages(mediaFiles = allMediaFiles, expiresHour = expiresHour)
            result.onSuccess { localItems ->
                // 成功時処理
                localItems.forEach { item ->
                    iclRepository.insertLocalItem(item)
                }
                urls.addAll(localItems.map { it.link })
                _uiState.update { it.copy(nowLoadingOption = NowLoadingOptions()) }
                navController.popBackStack(IclScreen.Home.name, inclusive = false)
                onResult(urls)
            }.onFailure {
                // 失敗時Dialog
                Log.e("IclViewModel", "Upload failed: ${it.message}")
                if (_uiState.value.selectedUploader == UploaderName.Imgur && _uiState.value.useImgurAccount) {
                    _uiState.update {
                        it.copy(
                            dialogOptions = DialogOptions(
                                isOpen = true,
                                title = R.string.dialog_title_upload_error,
                                body = R.string.dialog_body_imgur_token_expire,
                            )
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            dialogOptions = DialogOptions(
                                isOpen = true,
                                title = R.string.dialog_title_upload_error,
                                body = R.string.dialog_body_upload_error,
                            )
                        )
                    }
                }
                _uiState.update { it.copy(nowLoadingOption = NowLoadingOptions()) }
                onResult(emptyList())
            }
        }
    }

    // 画像縮小
    private suspend fun resizeImages(context: Context, uris: List<Uri>, maxSize: Int): List<File> {
        val resizedFiles = mutableListOf<File>()
        for (uri in uris) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(uri)
                    .size(maxSize)
                    .precision(Precision.INEXACT)
                    .build()

                val drawable = context.imageLoader.execute(request).drawable
                val bitmap = (drawable as? BitmapDrawable)?.bitmap

                bitmap?.let {
                    // 一時ファイルを作成
                    val tempFile =
                        File(context.cacheDir, "resized_${System.currentTimeMillis()}.jpg")
                    val outputStream = FileOutputStream(tempFile)

                    // JPEG形式で圧縮して保存
                    it.compress(Bitmap.CompressFormat.JPEG, 98, outputStream)
                    outputStream.close()

                    resizedFiles.add(tempFile)
                }
            } catch (e: Exception) {
                Log.e("IclViewModel", "Image resize failed: ${e.message}")
            }
        }
        return resizedFiles
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val mimeType = context.contentResolver.getType(uri)
            val extension = when (mimeType) {
                "image/jpeg" -> ".jpg"
                "image/png" -> ".png"
                "image/gif" -> ".gif"
                "video/mp4" -> ".mp4"
                "video/webm" -> ".webm"
                "video/quicktime" -> ".mov"
                else -> ".bin" // fallback
            }

            val tempFile = File.createTempFile("selected_image_", extension, context.cacheDir)
            tempFile.outputStream().use { output ->
                inputStream?.copyTo(output)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // サーバーから削除
    override fun deleteServerItem(item: LocalItem) {
        _uiState.update {
            it.copy(
                nowLoadingOption = NowLoadingOptions(
                    isOpen = true,
                    title = R.string.now_loading_delete
                )
            )
        }
        viewModelScope.launch {
            if (item.uploader == UploaderName.Imgur.toString()) {
                val isOk: Boolean = iclRepository.checkImgurCredits()
                if (!isOk) {
                    _uiState.update {
                        it.copy(
                            dialogOptions = DialogOptions(
                                isOpen = true,
                                title = R.string.dialog_title_delete_error,
                                body = R.string.dialog_body_imgur_api_error,
                            ),
                            nowLoadingOption = NowLoadingOptions()
                        )
                    }
                    return@launch;
                }
            }
            //
            val isSuccess: Boolean = if (item.uploader == UploaderName.Imgur.toString()) {
                iclRepository.deleteImgurItem(item)
            } else if (item.uploader == UploaderName.Catbox.toString()) {
                iclRepository.deleteCatboxItem(item)
            } else {
                false
            }
            if (isSuccess) {
                iclRepository.updateLocalItem(
                    item.copy(
                        isDeleted = true,
                    )
                )
            } else {
                _uiState.update {
                    it.copy(
                        dialogOptions = DialogOptions(
                            isOpen = true,
                            title = R.string.dialog_title_delete_error,
                            body = R.string.dialog_body_delete_error,
                        ),
                        nowLoadingOption = NowLoadingOptions()
                    )
                }
                return@launch;
            }
            //
            _uiState.update {
                it.copy(
                    nowLoadingOption = NowLoadingOptions()
                )
            }
        }
    }

    // 履歴アイテム削除
    override fun deleteLocalItem(item: LocalItem) {
        _uiState.update {
            it.copy(
                nowLoadingOption = NowLoadingOptions(
                    isOpen = true,
                    title = R.string.now_loading_delete
                )
            )
        }
        viewModelScope.launch {
            iclRepository.deleteLocalItem(item)
            _uiState.update {
                it.copy(
                    nowLoadingOption = NowLoadingOptions()
                )
            }
        }
    }

    // 履歴アイテム削除 imgur削除済み
    override fun deleteDeletedItems() {
        _uiState.update {
            it.copy(
                nowLoadingOption = NowLoadingOptions(
                    isOpen = true,
                    title = R.string.now_loading_delete
                )
            )
        }
        viewModelScope.launch {
            _uiState.value.localItems.forEach {
                if (it.isDeleted) {
                    iclRepository.deleteLocalItem(it)
                }
            }
            _uiState.update {
                it.copy(
                    nowLoadingOption = NowLoadingOptions()
                )
            }
        }
    }

    // 履歴アイテム削除 litterbox期限切れ
    override fun deleteExpiredLitterboxItems() {
        _uiState.update {
            it.copy(
                nowLoadingOption = NowLoadingOptions(
                    isOpen = true,
                    title = R.string.now_loading_delete
                )
            )
        }
        val nowTime = System.currentTimeMillis()
        viewModelScope.launch {
            _uiState.value.localItems.forEach {
                if (it.uploader == UploaderName.Litterbox.name && it.deleteAt !== null && it.deleteAt < nowTime) {
                    iclRepository.deleteLocalItem(it)
                }
            }
            _uiState.update {
                it.copy(
                    nowLoadingOption = NowLoadingOptions()
                )
            }
        }
    }

    // 履歴アイテム削除 全て
    override fun deleteAllLocalItems() {
        _uiState.update {
            it.copy(
                nowLoadingOption = NowLoadingOptions(
                    isOpen = true,
                    title = R.string.now_loading_delete
                )
            )
        }
        viewModelScope.launch {
            _uiState.value.localItems.forEach {
                iclRepository.deleteLocalItem(it)
            }
            _uiState.update {
                it.copy(
                    nowLoadingOption = NowLoadingOptions()
                )
            }
        }
    }

    // imgur アカウント情報更新
    override fun updateImgurAccountData(imgurAccountData: ImgurAccountData) {
        viewModelScope.launch {
            if (imgurAccountData.accessToken != null) {
                iclRepository.updateImgurAccessToken(imgurAccountData.accessToken)
            }
            if (imgurAccountData.name != null) {
                iclRepository.updateImgurAccountName(imgurAccountData.name)
            }
            if (imgurAccountData.expireAt != null) {
                iclRepository.updateImgurExpireAt(imgurAccountData.expireAt)
            }
        }
    }

    // imgur アカウント情報更新
    override fun deleteImgurAccountData() {
        viewModelScope.launch {
            updateUseImgurAccount(false)
            iclRepository.updateImgurAccessToken("")
            iclRepository.updateImgurAccountName("")
            iclRepository.updateImgurExpireAt(0)
        }
    }

    // imgur アカウント使用判定更新
    override fun updateUseImgurAccount(boolean: Boolean) {
        viewModelScope.launch {
            iclRepository.updateUseImgurAccount(boolean)
        }
    }

    // ランチャータイプ更新
    override fun updateImageLauncherType(launcherType: ImageLauncherType) {
        viewModelScope.launch {
            iclRepository.updateImageLauncherType(launcherType)
        }
    }

    override fun updateCatboxUserHash(hash: String) {
        viewModelScope.launch {
            iclRepository.updateCatboxUserHash(hash)
        }
    }


    //
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as IclMushroomApplication)
                IclViewModel(application.iclRepository)
            }
        }
    }
}

class MockIclViewModel : BaseIclViewModel {
    private val _uiState = MutableStateFlow(IclUiState())
    override val uiState: StateFlow<IclUiState> = _uiState.asStateFlow()

    override fun updateSelectedUploader(uploader: UploaderName) {}
    override fun updateUserClientId(clientId: String) {}
    override fun updateLocalClickOption(option: LocalClickOption) {}
    override fun updateIsDeleteExif(checked: Boolean) {}
    override fun setIsMushroom(boolean: Boolean) {}
    override fun onImagesSelected(
        context: Context,
        uris: List<Uri>,
        navController: NavController,
    ) {
    }

    override fun openDialog(dialogOptions: DialogOptions) {}
    override fun closeDialog() {}
    override fun uploadImages(
        context: Context,
        navController: NavController,
        reduceSize: Int?,
        expiresHour: Int,
        onResult: (List<String>) -> Unit,
    ) {
    }

    override fun deleteLocalItem(item: LocalItem) {}
    override fun deleteServerItem(item: LocalItem) {}
    override fun deleteDeletedItems() {}
    override fun deleteExpiredLitterboxItems() {}
    override fun deleteAllLocalItems() {}
    override fun updateIsCopyUrlAfterUpload(checked: Boolean) {}
    override fun setIsShared(boolean: Boolean) {}
    override fun updateImgurAccountData(imgurAccountData: ImgurAccountData) {}
    override fun deleteImgurAccountData() {}
    override fun updateUseImgurAccount(boolean: Boolean) {}
    override fun openConfirmDialog(dialogOptions: DialogOptions) {}
    override fun closeConfirmDialog() {}
    override fun updateImageLauncherType(launcherType: ImageLauncherType) {}
    override fun updateCatboxUserHash(hash: String) {}

    fun updateSelectedFiles(files: List<Uri>) {
        _uiState.update { it.copy(selectedFiles = files) }
    }
}
