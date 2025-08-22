package com.example.myapplication.ui.addrequest

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ManageRequestsScreen(
    navController: NavController,
    requestRepository: RequestDaoRepository,
    settingsRepository: SettingsRepository
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val scope = rememberCoroutineScope()

    // VM che ricavano internamente userId da SettingsRepository (NON lo passiamo)
    val createdVM: UserRequestListViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = UserRequestListViewModelFactory(
                repository = requestRepository,
                settingsRepository = settingsRepository
            )
        )

    val addVM: AddRequestViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = AddRequestViewModelFactory(
                repository = requestRepository,
                settingsRepository = settingsRepository
            )
        )

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text("Richieste create") }
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text("Nuova richiesta") }
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> UserRequestsList(
                    navController = navController,
                    viewModel = createdVM
                )
                1 -> AddRequestScreen(
                    viewModel = addVM,
                    onCreated = {
                        // dopo creazione torniamo alla lista
                        scope.launch { pagerState.animateScrollToPage(0) }
                    }
                )
            }
        }
    }
}