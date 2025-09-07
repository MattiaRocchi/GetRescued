package com.example.myapplication.ui.slide

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.database.Request
import com.example.myapplication.data.database.Tags
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.data.repositories.TagsRepository
import com.example.myapplication.ui.GetRescuedRoute
import com.example.myapplication.ui.composables.DynamicRequestCard
import com.example.myapplication.ui.composables.RequestFilter
import com.example.myapplication.ui.participationrequests.ParticipatingRequestsViewModel
import com.example.myapplication.ui.requests.RequestsViewModel
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

    val allValidRequest: RequestsViewModel = koinViewModel()
    val participatingVR: ParticipatingRequestsViewModel = koinViewModel()
    val settingsRepository: SettingsRepository = koinInject()
    val tagsRepository: TagsRepository = koinInject()

    // Ottieni l'ID utente corrente
    val currentUserId by settingsRepository.userIdFlow.collectAsState(initial = -1)

    // Stati per i filtri
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }
    var selectedTags by remember { mutableStateOf<List<Tags>>(emptyList()) }
    var sortByDate by remember { mutableStateOf(true) } // true = più recenti, false = più vecchi
    var hideMyRequests by remember { mutableStateOf(false) }

    // Carica tutti i tag disponibili
    val availableTags by tagsRepository.allTagsFlow().collectAsState(initial = emptyList())

    // Mappa per associare request ID ai loro tags
    var requestTagsMap by remember { mutableStateOf<Map<Int, List<Tags>>>(emptyMap()) }

    // Carica i tags per le richieste quando cambiano
    val allRequests by allValidRequest.availableRequests.collectAsState()
    val rawParticipatingRequests by participatingVR.participation.collectAsState()
    val participatingRequests = rawParticipatingRequests.filter { !it.completed }


    LaunchedEffect(allRequests, participatingRequests) {
        val allRequestIds = (allRequests + participatingRequests).map { it.id }.distinct()
        val newTagsMap = mutableMapOf<Int, List<Tags>>()

        allRequestIds.forEach { requestId ->
            try {
                val tags = tagsRepository.getTagsForRequest(requestId)
                newTagsMap[requestId] = tags
            } catch (e: Exception) {
                // Se c'è un errore nel caricamento dei tags, usa lista vuota
                newTagsMap[requestId] = emptyList()
            }
        }

        requestTagsMap = newTagsMap
    }

    Column(Modifier.fillMaxSize()) {
        // Filtro completo con tutti i parametri
        RequestFilter(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            selectedDifficulty = selectedDifficulty,
            onDifficultyChange = { selectedDifficulty = it },
            selectedTags = selectedTags,
            onTagsChange = { selectedTags = it },
            availableTags = availableTags,
            sortByDate = sortByDate,
            onSortByDateChange = { sortByDate = it },
            hideMyRequests = hideMyRequests,
            onHideMyRequestsChange = { hideMyRequests = it },
            showFilters = showFilters,
            onToggleFilters = { showFilters = !showFilters },
            modifier = Modifier.padding(16.dp)
        )

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
        Spacer(Modifier.height(12.dp))


        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> FilteredRequestsList(
                    requests = allRequests,
                    requestTagsMap = requestTagsMap,
                    searchQuery = searchQuery,
                    selectedDifficulty = selectedDifficulty,
                    selectedTags = selectedTags,
                    sortByDate = sortByDate,
                    hideMyRequests = hideMyRequests,
                    navController = navController,
                    currentUserId = currentUserId
                )
                1 -> FilteredRequestsList(
                    requests = participatingRequests,
                    requestTagsMap = requestTagsMap,
                    searchQuery = searchQuery,
                    selectedDifficulty = selectedDifficulty,
                    selectedTags = selectedTags,
                    sortByDate = sortByDate,
                    hideMyRequests = hideMyRequests,
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
    requestTagsMap: Map<Int, List<Tags>>,
    searchQuery: String,
    selectedDifficulty: String?,
    selectedTags: List<Tags>,
    sortByDate: Boolean,
    hideMyRequests: Boolean,
    navController: NavController,
    currentUserId: Int
) {
    val filteredRequests = remember(
        requests,
        requestTagsMap,
        searchQuery,
        selectedDifficulty,
        selectedTags,
        sortByDate,
        hideMyRequests,
        currentUserId
    ) {
        requests
            .filter { request ->
                // Filtro per testo di ricerca
                val matchesSearch = searchQuery.isBlank() ||
                        request.title.contains(searchQuery, ignoreCase = true) ||
                        request.description.contains(searchQuery, ignoreCase = true)

                // Filtro per difficoltà
                val matchesDifficulty =
                    selectedDifficulty == null || request.difficulty == selectedDifficulty

                // Filtro per tag richiesti
                val requestTags = requestTagsMap[request.id] ?: emptyList()
                val matchesTags = selectedTags.isEmpty() ||
                        selectedTags.all { selectedTag ->
                            requestTags.any { requestTag -> requestTag.id == selectedTag.id }
                        }

                //Filtro per nascondere le proprie richieste
                val matchesOwnership = !hideMyRequests || (currentUserId != request.sender)

                matchesSearch && matchesDifficulty && matchesTags && matchesOwnership
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
                val requestTags = requestTagsMap[request.id] ?: emptyList()
                DynamicRequestCard(
                    request = request,
                    tags = requestTags,
                    onClick = { navController.navigate(GetRescuedRoute.InfoRequest(request.id)) },
                    currentUserId = currentUserId
                )
            }
        }
    }
}