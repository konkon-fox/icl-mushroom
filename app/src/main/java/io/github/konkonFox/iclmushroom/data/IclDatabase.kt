package io.github.konkonFox.iclmushroom.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LocalItem::class], version = 1, exportSchema = false)
abstract class IclDatabase : RoomDatabase() {

    abstract fun localItemDao(): LocalItemDao

    companion object {
        @Volatile
        private var Instance: IclDatabase? = null

        fun getDatabase(context: Context): IclDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, IclDatabase::class.java, "item_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}