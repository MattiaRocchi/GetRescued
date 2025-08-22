package com.example.myapplication

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.TitleBadgeRepository
import com.example.myapplication.data.repositories.UserDaoRepository
import com.example.myapplication.ui.addrequest.AddRequestViewModel
import com.example.myapplication.ui.changeProfile.ChangeProfileScreen
import com.example.myapplication.ui.changeProfile.ChangeProfileViewModel
import com.example.myapplication.ui.inforequest.InfoRequestViewModel
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.profile.ProfileViewModel
import com.example.myapplication.ui.profile.loadTitleBadgesFromRaw
import com.example.myapplication.ui.registration.RegistrationViewModel
import com.example.myapplication.ui.requests.RequestsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore by preferencesDataStore("settings")

val appModule = module {

    // DataStore singleton
    single { androidContext().dataStore }

    // Room Database singleton + precaricamento TitleBadge
    single {
        val context = androidContext()
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "getrescued-db"
        )
            .fallbackToDestructiveMigration()
            .build()

        // Precarica i TitleBadge dal JSON in res/raw
        CoroutineScope(Dispatchers.IO).launch {
            if (db.titleBadgeDao().getAll().isEmpty()) {
                val titles = loadTitleBadgesFromRaw(context)
                if (titles.isNotEmpty()) {
                    db.titleBadgeDao().insertAll(titles)
                }
            }
        }

        db
    }

    // DAO bindings
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().requestDao() }
    single { get<AppDatabase>().titleBadgeDao() }

    // Repository bindings
    single { UserDaoRepository(get()) }
    single { RequestDaoRepository(get()) }
    single { TitleBadgeRepository(get()) }
    single { SettingsRepository(get(), get()) }

    // ViewModels
    viewModel { RegistrationViewModel(get(), get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { AddRequestViewModel(get()) } // RequestDaoRepository
    viewModel { RequestsViewModel(get()) }   // RequestDaoRepository
    viewModel { InfoRequestViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get()) }
    viewModel { ChangeProfileViewModel(get(), get()) }
}
