package com.example.myapplication

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.TitleBadgeRepository
import com.example.myapplication.data.repositories.UserDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.ui.SettingsScreen.SettingsViewModel
import com.example.myapplication.ui.addrequest.AddRequestViewModel
import com.example.myapplication.ui.changeProfile.ChangeProfileViewModel
import com.example.myapplication.ui.addrequest.UserRequestListViewModel
import com.example.myapplication.ui.editrequest.EditRequestViewModel
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.requests.RequestsViewModel
import com.example.myapplication.ui.registration.RegistrationViewModel
import com.example.myapplication.ui.inforequest.InfoRequestViewModel
import com.example.myapplication.ui.profile.ProfileViewModel
import com.example.myapplication.ui.profile.loadTitleBadgesFromRaw
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

    // Room Database singleton
    single {
        val ctx = androidContext()

        val db = Room.databaseBuilder(
            ctx,
            AppDatabase::class.java,
            "getrescued-db"
        )
            .fallbackToDestructiveMigration()
            .build()

        // Precarica i TitleBadge dal JSON in res/raw se il DB è vuoto.
        // Nota: questo è lanciato in background e non blocca la creazione del singleton.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Se hai una query count() sul DAO sarebbe preferibile usarla
                val isEmpty = db.titleBadgeDao().getAll().isEmpty()
                if (isEmpty) {
                    val titles = loadTitleBadgesFromRaw(ctx)
                    if (titles.isNotEmpty()) {
                        db.titleBadgeDao().insertAll(titles)
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
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
    viewModel { AddRequestViewModel(get(), get()) }
    viewModel { RequestsViewModel(get()) }
    viewModel { InfoRequestViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get()) }
    viewModel { EditRequestViewModel(get(), get()) }
    viewModel { UserRequestListViewModel(get(), get()) }
    viewModel { ChangeProfileViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
}
