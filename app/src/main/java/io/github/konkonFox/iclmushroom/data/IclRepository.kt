package io.github.konkonFox.iclmushroom.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.konkonFox.iclmushroom.LocalClickOption
import io.github.konkonFox.iclmushroom.UploaderName
import io.github.konkonFox.iclmushroom.model.ImgurCreditsResponse
import io.github.konkonFox.iclmushroom.model.ImgurDeleteResponse
import io.github.konkonFox.iclmushroom.model.ImgurUploadResponse
import io.github.konkonFox.iclmushroom.model.MediaFile
import io.github.konkonFox.iclmushroom.network.CatboxApiService
import io.github.konkonFox.iclmushroom.network.ImgurApiService
import io.github.konkonFox.iclmushroom.network.LitterboxApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.IOException
import java.net.URLConnection

class IclRepository(
    private val dataStore: DataStore<androidx.datastore.preferences.core.Preferences>,
    private val localItemDao: LocalItemDao,
) {
    private companion object {
        val SELECTED_UPLOADER = stringPreferencesKey("selected_uploader")
        val USER_CLIENT_ID = stringPreferencesKey("user_client_id")
        val LOCAL_CLICK_OPTION = stringPreferencesKey("local_click_option")
        val IS_DELETE_EXIF = booleanPreferencesKey("is_delete_exif")
        const val TAG = "IclRepository"
    }

    // selected_uploader の取得
    val selectedUploader: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[SELECTED_UPLOADER] ?: UploaderName.Imgur.name // デフォルト値
        }

    // user_client_id の取得
    val userClientId: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[USER_CLIENT_ID] ?: "" // デフォルト値
        }

    // local_click_optionの取得
    val localClickOption: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[LOCAL_CLICK_OPTION] ?: LocalClickOption.Insert.name // デフォルト値
        }

    // is_delete_exif の取得
    val isDeleteExif: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[IS_DELETE_EXIF] == true // デフォルト値
        }

    // selected_uploader の保存
    suspend fun updateSelectedUploader(uploader: UploaderName) {
        dataStore.edit { preferences ->
            preferences[SELECTED_UPLOADER] = uploader.name
        }
    }

    // user_client_id の保存
    suspend fun updateUserClientId(clientId: String) {
        dataStore.edit { preferences ->
            preferences[USER_CLIENT_ID] = clientId
        }
    }

    // local_click_optionの保存
    suspend fun updateLocalClickOption(option: LocalClickOption) {
        dataStore.edit { preferences ->
            preferences[LOCAL_CLICK_OPTION] = option.name
        }
    }

    // is_delete_exif の保存
    suspend fun updateIsDeleteExif(checked: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DELETE_EXIF] = checked
        }
    }

    // 画像アップロード統括
    suspend fun uploadImages(
        mediaFiles: List<MediaFile>,
        expiresHour: Int,
    ): Result<List<LocalItem>> {
        return try {
            val uploader = selectedUploader.first()
            when (uploader) {
                UploaderName.Imgur.name -> uploadImageToImgur(mediaFiles)
                UploaderName.Catbox.name -> uploadImageToCatbox(mediaFiles)
                UploaderName.Litterbox.name -> uploadImageToLitterbox(
                    mediaFiles = mediaFiles,
                    expiresHour = expiresHour
                )

                else -> Result.failure(Exception("Unsupported uploader: $selectedUploader"))
            }
        } catch (e: Exception) {
            Log.e("IclRepository", "uploadImages failed: ${e.message}")
            Result.failure(e)
        }
    }

    // imgur clientID
    private suspend fun getClientId(): String {
        val userId = userClientId.first()
        return if (userId.isEmpty()) {
            ImgurConfig.getClientId()
        } else {
            userId
        }
    }

    // imgur limit 確認
    suspend fun checkImgurCredits(): Boolean {
        val clientId: String = getClientId()
        val authHeader = "Client-ID $clientId"
        try {
            val response: ImgurCreditsResponse = ImgurApiService.api.getCredits(
                authHeader = authHeader
            )
            Log.d("IclRepository", response.data.userRemaining.toString())
            Log.d("IclRepository", response.data.clientRemaining.toString())
            return response.data.userRemaining > 100 && response.data.clientRemaining > 1250
        } catch (e: HttpException) {
            Log.e(
                "IclRepository",
                "Check credits failed message: $e"
            )
            return false
        } catch (e: Exception) {
            Log.e(
                "IclRepository",
                "Check credits failed message: $e"
            )
            return false
        }
        return true
    }

    // imgurにアップロード
    private suspend fun uploadImageToImgur(mediaFiles: List<MediaFile>): Result<List<LocalItem>> {
        val localItems = mutableListOf<LocalItem>()
        val uploader = selectedUploader.first()
        val clientId: String = getClientId()
        val authHeader = "Client-ID $clientId"
        // upload
        mediaFiles.forEach { mediaFile ->
            val (isVideo, file) = mediaFile
            try {
                val requestFile = file.asRequestBody("image/*".toMediaType())
                val imagePart =
                    MultipartBody.Part.createFormData("image", file.name, requestFile)
                val response: ImgurUploadResponse = ImgurApiService.api.postImage(
                    image = imagePart,
                    authHeader = authHeader
                )
                localItems.add(
                    LocalItem(
                        uploader = uploader,
                        link = response.data.link,
                        isDeleted = false,
                        deleteHash = response.data.deleteHash,
                        createdAt = System.currentTimeMillis(),
                        deleteAt = null,
                        isVideo = isVideo
                    )
                )
            } catch (e: HttpException) {
                Log.e(
                    "IclRepository",
                    "Upload failed for ${file.name}: ${e.response()?.errorBody()?.string()}"
                )
            } catch (e: Exception) {
                Log.e("IclRepository", "Upload failed for ${file.name}: ${e.message}")
            }
        }
        return if (localItems.isEmpty()) {
            Result.failure(Exception("All uploads failed"))
        } else {
            Result.success(localItems)
        }
    }

    // catboxにアップロード
    private suspend fun uploadImageToCatbox(mediaFiles: List<MediaFile>): Result<List<LocalItem>> {
        val localItems = mutableListOf<LocalItem>()
        val uploader = selectedUploader.first()

        mediaFiles.forEach { mediaFile ->
            val (isVideo, file) = mediaFile
            try {
                val mimeType =
                    URLConnection.guessContentTypeFromName(file.name) ?: "application/octet-stream"
                val requestFile = file.asRequestBody(mimeType.toMediaType())
                val reqTypePart = MultipartBody.Part.createFormData("reqtype", "fileupload")
                val imagePart =
                    MultipartBody.Part.createFormData("fileToUpload", file.name, requestFile)
                val url = CatboxApiService.api.postImage(
                    reqtype = reqTypePart,
                    image = imagePart
                )
                localItems.add(
                    LocalItem(
                        uploader = uploader,
                        link = url,
                        isDeleted = false,
                        deleteHash = null,
                        createdAt = System.currentTimeMillis(),
                        deleteAt = null,
                        isVideo = isVideo
                    )
                )
            } catch (e: HttpException) {
                Log.e(
                    "IclRepository",
                    "Upload failed for ${file.name}: ${e.response()?.errorBody()?.string()}"
                )
            } catch (e: Exception) {
                Log.e("IclRepository", "Upload failed for ${file.name}: ${e.message}")
            }
        }
        return if (localItems.isEmpty()) {
            Result.failure(Exception("All uploads failed"))
        } else {
            Result.success(localItems)
        }
    }


    // litterboxにアップロード
    private suspend fun uploadImageToLitterbox(
        mediaFiles: List<MediaFile>,
        expiresHour: Int,
    ): Result<List<LocalItem>> {
        val localItems = mutableListOf<LocalItem>()
        val uploader = selectedUploader.first()
        mediaFiles.forEach { mediaFile ->
            val (isVideo, file) = mediaFile
            try {
                val mimeType =
                    URLConnection.guessContentTypeFromName(file.name) ?: "application/octet-stream"
                Log.d("IclRepository", mimeType)
                val requestFile = file.asRequestBody(mimeType.toMediaType())
                val reqTypePart = MultipartBody.Part.createFormData("reqtype", "fileupload")
                val timePart = MultipartBody.Part.createFormData("time", "${expiresHour}h")
                val imagePart =
                    MultipartBody.Part.createFormData("fileToUpload", file.name, requestFile)
                val url = LitterboxApiService.api.postImage(
                    reqtype = reqTypePart,
                    time = timePart,
                    image = imagePart
                )
                val createdAt: Long = System.currentTimeMillis()
                val deleteAt: Long = createdAt + expiresHour * 60 * 60 * 1000
                localItems.add(
                    LocalItem(
                        uploader = uploader,
                        link = url,
                        isDeleted = false,
                        deleteHash = null,
                        createdAt = createdAt,
                        deleteAt = deleteAt,
                        isVideo = isVideo
                    )
                )
            } catch (e: HttpException) {
                Log.e(
                    "IclRepository",
                    "Upload failed for ${file.name}: ${e.response()?.errorBody()?.string()}"
                )
            } catch (e: Exception) {
                Log.e("IclRepository", "Upload failed for ${file.name}: ${e.message}")
            }
        }
        return if (localItems.isEmpty()) {
            Result.failure(Exception("All uploads failed"))
        } else {
            Result.success(localItems)
        }
    }

    // imgurから削除
    suspend fun deleteImgurItem(item: LocalItem): Boolean {
        val clientId: String = getClientId()
        val authHeader = "Client-ID $clientId"
        try {
            val deleteHash = item.deleteHash
            if (deleteHash == null) throw Exception("no deleteHash")
            val response: ImgurDeleteResponse = ImgurApiService.api.deleteImage(
                authHeader = authHeader,
                deleteHash = deleteHash
            )
            Log.d("IclRepository", "response $response")
            return response.success
        } catch (e: HttpException) {
            Log.e(
                "IclRepository",
                "Delete failed for : ${e.response()?.errorBody()?.string()}"
            )
            return false
        } catch (e: Exception) {
            Log.e("IclRepository", "Delete failed for : ${e.message}")
            return false
        }
    }

    // 画像登録
    suspend fun insertLocalItem(item: LocalItem) {
        localItemDao.insert(item)
    }

    // 画像取得
    fun getAllLocalItems(): Flow<List<LocalItem>> {
        return localItemDao.getAllItems()
    }

    // 画像削除
    suspend fun deleteLocalItem(item: LocalItem) {
        localItemDao.delete(item)
    }

    // 画像更新
    suspend fun updateLocalItem(item: LocalItem) {
        localItemDao.update(item)
    }
}