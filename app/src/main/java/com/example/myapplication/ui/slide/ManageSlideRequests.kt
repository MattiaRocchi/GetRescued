package com.example.myapplication.ui.slide

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.myapplication.ui.addrequest.AddRequestScreen
import com.example.myapplication.ui.addrequest.AddRequestViewModel
import com.example.myapplication.ui.userrequest.UserRequestListViewModel
import com.example.myapplication.ui.userrequest.UserRequestsList
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ManageSlideRequests(
    navController: NavController
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val createdVM: UserRequestListViewModel = koinViewModel()
    val addVM: AddRequestViewModel = koinViewModel()

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