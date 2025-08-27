package com.example.myapplication

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.repositories.MissionRepository
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.TitleBadgeRepository
import com.example.myapplication.data.repositories.UserDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.TagsRepository
import com.example.myapplication.ui.SettingsScreen.SettingsViewModel
import com.example.myapplication.ui.addrequest.AddRequestViewModel
import com.example.myapplication.ui.changeProfile.ChangeProfileViewModel
import com.example.myapplication.ui.userrequest.UserRequestListViewModel
import com.example.myapplication.ui.editrequest.EditRequestViewModel
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.requests.RequestsViewModel
import com.example.myapplication.ui.registration.RegistrationViewModel
import com.example.myapplication.ui.inforequest.InfoRequestViewModel
import com.example.myapplication.ui.missions.MissionViewModel
import com.example.myapplication.ui.participationrequests.ParticipatingRequestsViewModel
import com.example.myapplication.ui.profile.ProfileViewModel
import com.example.myapplication.utils.loadMissionsFromRaw
import com.example.myapplication.utils.loadTagsFromRaw
import com.example.myapplication.utils.loadTitleBadgesFromRaw
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
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
                val isEmpty = db.tagDao().getAllTags().firstOrNull().isNullOrEmpty() // o una count sync se preferisci
                if (isEmpty) {
                    val tags = loadTagsFromRaw(ctx)
                    if (tags.isNotEmpty()) db.tagDao().insertAll(tags)
                }
            } catch (t: Throwable) { t.printStackTrace() }
        }

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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isEmpty = db.missionDao().getAll().isEmpty()
                if (isEmpty) {
                    val general = loadMissionsFromRaw(ctx, true)
                    val weekly = loadMissionsFromRaw(ctx, false)
                    val allMissions = general + weekly

                    if (allMissions.isNotEmpty()) {
                        db.missionDao().insertAll(allMissions)
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
    single { get<AppDatabase>().missionDao() }
    single { get<AppDatabase>().tagDao() }

    // Repository bindings
    single { UserDaoRepository(get()) }
    single { RequestDaoRepository(get()) }
    single { TitleBadgeRepository(get()) }
    single { SettingsRepository(get(), get()) }
    single { MissionRepository(get()) }
    single { TagsRepository(get()) }


    // ViewModels
    viewModel { RegistrationViewModel(get(), get(), get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { AddRequestViewModel(get(), get(), get()) } // get() → RequestDaoRepository
    viewModel { RequestsViewModel(get()) }   // get() → RequestDaoRepository
    viewModel { RequestsViewModel(get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
    viewModel { UserRequestListViewModel(get(), get()) }
    viewModel { ChangeProfileViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { ParticipatingRequestsViewModel(get(), get()) }
    viewModel { MissionViewModel(get(), get(),
        get(),get()) }

    // ViewModel con parametri - usando parametersOf
    viewModel { (requestId: Int) ->
        InfoRequestViewModel(
            requestRepository = get(),
            userDaoRepository = get(),
            settingsRepository = get(),
            requestId = requestId
        )
    }

    viewModel { (requestId: Int) ->
        EditRequestViewModel(
            repository = get(),
            titleBadgeRepository = get(),
            requestId = requestId
        )
    }

}
