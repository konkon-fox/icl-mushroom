package io.github.konkonFox.iclmushroom

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import io.github.konkonFox.iclmushroom.data.IclDatabase
import io.github.konkonFox.iclmushroom.data.IclRepository

private const val PREFERENCES_NAME = "icl_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCES_NAME
)

class IclMushroomApplication : Application() {
    lateinit var iclRepository: IclRepository
    val database: IclDatabase by lazy { IclDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        iclRepository = IclRepository(
            dataStore = dataStore,
            localItemDao = database.localItemDao()
        )
    }
}