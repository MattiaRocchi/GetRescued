package com.example.myapplication.ui.slide

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.myapplication.ui.participationrequests.ParticipatingRequests
import com.example.myapplication.ui.participationrequests.ParticipatingRequestsViewModel
import com.example.myapplication.ui.requests.RequestsScreen
import com.example.myapplication.ui.requests.RequestsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BrowseRequestsScreen(
    navController: NavController
    // NON PIÙ parametri repository/settings - Koin gestisce tutto!
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val scope = rememberCoroutineScope()

    // SEMPLICISSIMO - tutto tramite Koin
    val allVM: RequestsViewModel = koinViewModel()
    val participatingVM: ParticipatingRequestsViewModel = koinViewModel()

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
