package com.example.myapplication

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.myapplication.data.repositories.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.TitleBadgeRepository
import com.example.myapplication.data.repositories.UserDaoRepository
import com.example.myapplication.data.utils.Converters

val Context.dataStore by preferencesDataStore("settings")

val appModule = module {

    // DataStore singleton
    single { androidContext().dataStore }

    // Room Database singleton
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "getrescued-db"
        )
            .addTypeConverter(Converters()) // opzionale se non hai già usato @TypeConverters nella classe DB
            .build()
    }

    // DAO bindings
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().requestDao() }
    single { get<AppDatabase>().titleBadgeDao() }

    // Repository bindings
    single { UserDaoRepository(get()) }
    single { RequestDaoRepository(get()) }
    single { TitleBadgeRepository(get()) }
    single { SettingsRepository(get()) }

    //ViewModes
}
