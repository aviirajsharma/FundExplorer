package com.avirajsharma.fundexplorer.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.avirajsharma.fundexplorer.data.model.FundSearchResult
import com.avirajsharma.fundexplorer.ui.components.AddToWatchlistDialog
import com.avirajsharma.fundexplorer.ui.components.LoadingState
import com.avirajsharma.fundexplorer.ui.components.ErrorState
import com.avirajsharma.fundexplorer.ui.viewmodel.FundViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundDetailScreen(
    schemeCode: Int,
    viewModel: FundViewModel,
    onBackClick: () -> Unit
) {
    val fundDetails by viewModel.fundDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val watchlistFolders by viewModel.watchlistFolders.collectAsState()
    
    var showAddToWatchlistDialog by remember { mutableStateOf(false) }

    LaunchedEffect(schemeCode) {
        viewModel.fetchFundDetails(schemeCode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fund Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddToWatchlistDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add to Watchlist")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (isLoading) {
                LoadingState()
            } else if (error != null) {
                ErrorState(message = error ?: "Failed to load details", onRetry = { viewModel.fetchFundDetails(schemeCode) })
            } else {
                fundDetails?.let { details ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = details.meta.schemeName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Fund House: ${details.meta.fundHouse}", style = MaterialTheme.typography.bodyLarge)
                            Text(text = "Category: ${details.meta.schemeCategory}", style = MaterialTheme.typography.bodyLarge)
                            Text(text = "Type: ${details.meta.schemeType}", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "NAV History",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(details.navHistory) { navEntry ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = navEntry.date)
                                    Text(text = "₹ ${navEntry.nav}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (showAddToWatchlistDialog) {
            fundDetails?.let { details ->
                AddToWatchlistDialog(
                    folders = watchlistFolders,
                    onDismiss = { showAddToWatchlistDialog = false },
                    onFolderSelected = { folderId ->
                        viewModel.addFundToWatchlist(
                            folderId, 
                            FundSearchResult(schemeCode, details.meta.schemeName)
                        )
                        showAddToWatchlistDialog = false
                    },
                    onCreateFolder = { name ->
                        viewModel.createWatchlistFolder(name)
                    }
                )
            }
        }
    }
}
