package com.example.myapplication.ui.requests

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.repositories.RequestDaoRepository
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.ui.participationrequests.ParticipatingRequests
import com.example.myapplication.ui.participationrequests.ParticipatingRequestsViewModel
import com.example.myapplication.ui.participationrequests.ParticipatingRequestsViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BrowseRequestsScreen(
    navController: NavController,
    requestRepository: RequestDaoRepository,
    settingsRepository: SettingsRepository
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val scope = rememberCoroutineScope()

    val allVM: RequestsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = RequestsViewModelFactory(requestRepository)
    )
    val participatingVM: ParticipatingRequestsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ParticipatingRequestsViewModelFactory(
            requestRepository = requestRepository,
            settingsRepository = settingsRepository
        )
    )

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text("Tutte le richieste") }
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text("A cui partecipo") }
            )
        }

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> RequestsScreen(navController = navController, viewModel = allVM)
                1 -> ParticipatingRequests(navController = navController, viewModel = participatingVM)
            }
        }
    }
}