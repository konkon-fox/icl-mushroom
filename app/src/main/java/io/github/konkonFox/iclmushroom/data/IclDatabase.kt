package io.github.konkonFox.iclmushroom.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [LocalItem::class], version = 2, exportSchema = false)
abstract class IclDatabase : RoomDatabase() {

    abstract fun localItemDao(): LocalItemDao

    companion object {
        @Volatile
        private var Instance: IclDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE items ADD COLUMN isVideo INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): IclDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, IclDatabase::class.java, "item_database")
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}