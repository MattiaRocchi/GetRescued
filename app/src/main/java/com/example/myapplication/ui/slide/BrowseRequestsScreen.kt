package com.example.myapplication.ui.slide

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.ui.GetRescuedRoute
import com.example.myapplication.ui.composables.DynamicRequestCard
import com.example.myapplication.ui.participationrequests.ParticipatingRequestsViewModel
import com.example.myapplication.ui.requests.RequestsViewModel
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BrowseRequestsScreen(
    navController: NavController
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val scope = rememberCoroutineScope()

    val allVM: RequestsViewModel = koinViewModel()
    val participatingVM: ParticipatingRequestsViewModel = koinViewModel()
    val settingsRepository: SettingsRepository = koinInject()

    // Ottieni l'ID utente corrente
    val currentUserId by settingsRepository.userIdFlow.collectAsState(initial = -1)

    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }
    var sortByDate by remember { mutableStateOf(true) } // true = più recenti, false = più vecchi

    Column(Modifier.fillMaxSize()) {
        // Barra di ricerca
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Cerca richieste...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Cerca") },
                    trailingIcon = {
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(Icons.Default.FilterList, "Filtri")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (showFilters) {
                    Spacer(Modifier.height(12.dp))

                    // Filtro difficoltà
                    Text("Difficoltà:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        FilterChip(
                            onClick = { selectedDifficulty = if (selectedDifficulty == "Bassa") null else "Bassa" },
                            label = { Text("Bassa") },
                            selected = selectedDifficulty == "Bassa",
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EasyTask
                            )
                        )
                        FilterChip(
                            onClick = { selectedDifficulty = if (selectedDifficulty == "Media") null else "Media" },
                            label = { Text("Media") },
                            selected = selectedDifficulty == "Media",
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MediumTask
                            )
                        )
                        FilterChip(
                            onClick = { selectedDifficulty = if (selectedDifficulty == "Alta") null else "Alta" },
                            label = { Text("Alta") },
                            selected = selectedDifficulty == "Alta",
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DifficulTask
                            )
                        )
                    }

                    // Ordinamento per data
                    Text("Ordinamento:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        FilterChip(
                            onClick = { sortByDate = true },
                            label = { Text("Più recenti") },
                            selected = sortByDate
                        )
                        FilterChip(
                            onClick = { sortByDate = false },
                            label = { Text("Più vecchi") },
                            selected = !sortByDate
                        )
                    }
                }
            }
        }

        TabRow(selectedTabIndex = pagerState.currentPage) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text("Disponibili") }
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text("A cui partecipo") }
            )
        }

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> FilteredRequestsList(
                    requests = allVM.availableRequests.collectAsState().value,
                    searchQuery = searchQuery,
                    selectedDifficulty = selectedDifficulty,
                    sortByDate = sortByDate,
                    navController = navController,
                    currentUserId = currentUserId
                )
                1 -> FilteredRequestsList(
                    requests = participatingVM.participation.collectAsState().value,
                    searchQuery = searchQuery,
                    selectedDifficulty = selectedDifficulty,
                    sortByDate = sortByDate,
                    navController = navController,
                    currentUserId = currentUserId
                )
            }
        }
    }
}

@Composable
private fun FilteredRequestsList(
    requests: List<Request>,
    searchQuery: String,
    selectedDifficulty: String?,
    sortByDate: Boolean,
    navController: NavController,
    currentUserId: Int
) {
    val filteredRequests = remember(requests, searchQuery, selectedDifficulty, sortByDate) {
        requests
            .filter { request ->
                // Filtro per testo di ricerca
                val matchesSearch = searchQuery.isBlank() ||
                        request.title.contains(searchQuery, ignoreCase = true) ||
                        request.description.contains(searchQuery, ignoreCase = true)

                // Filtro per difficoltà
                val matchesDifficulty = selectedDifficulty == null || request.difficulty == selectedDifficulty

                matchesSearch && matchesDifficulty
            }
            .sortedBy { if (sortByDate) -it.date else it.date }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (filteredRequests.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nessuna richiesta trovata",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            items(filteredRequests) { request ->
                DynamicRequestCard(
                    request = request,
                    onClick = { navController.navigate(GetRescuedRoute.InfoRequest(request.id)) },
                    currentUserId = currentUserId
                )
            }
        }
    }
}