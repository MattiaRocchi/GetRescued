package com.example.myapplication.ui.userrequest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.database.PendingRequest
import com.example.myapplication.data.database.Tags
import com.example.myapplication.data.repositories.SettingsRepository
import com.example.myapplication.ui.GetRescuedRoute
import com.example.myapplication.ui.composables.DynamicRequestCard
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

@Composable
fun UserRequestsList(
    navController: NavController,
    viewModel: UserRequestListViewModel
) {
    val request by viewModel.myRequests.collectAsState()
    val filterRequests = request.filter { !it.completed }
    val settingsRepository: SettingsRepository = koinInject()
    val currentUserId by settingsRepository.userIdFlow.collectAsState(initial = -1)

    // Stati per gestire tag e proposte di ogni richiesta
    val scope = rememberCoroutineScope()
    var requestTags by remember { mutableStateOf<Map<Int, List<Tags>>>(emptyMap()) }
    var requestPendingRequests by remember { mutableStateOf<Map<Int, List<PendingRequest>>>(emptyMap()) }

    //Effetto per caricare tag e proposte quando le richieste cambiano
    LaunchedEffect(filterRequests) {
        scope.launch {
            val newTags = mutableMapOf<Int, List<Tags>>()
            val newPendingRequests = mutableMapOf<Int, List<PendingRequest>>()

            filterRequests.forEach { request ->
                newTags[request.id] = viewModel.getTagsForRequest(request.id)
                newPendingRequests[request.id] = viewModel.getPendingRequestsForRequest(request.id)
            }

            requestTags = newTags
            requestPendingRequests = newPendingRequests
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (filterRequests.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Non hai ancora creato richieste.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            items(filterRequests) { request ->
                //Passa anche tag e proposte
                DynamicRequestCard(
                    request = request,
                    tags = requestTags[request.id] ?: emptyList(),
                    pendingRequests = requestPendingRequests[request.id] ?: emptyList(),
                    onClick = { navController.navigate(GetRescuedRoute.ManageRequestDetails(request.id)) },
                    currentUserId = currentUserId
                )
            }
        }
    }
}