package io.github.konkonFox.iclmushroom.data


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class LocalItem(
    // 1
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uploader: String,
    val link: String,
    val isDeleted: Boolean,
    val deleteHash: String?,
    val createdAt: Long,
    val deleteAt: Long?,
    // 2
    val isVideo: Boolean = false,
)