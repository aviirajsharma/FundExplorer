package com.avirajsharma.fundexplorer.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.avirajsharma.fundexplorer.data.model.FundSearchResult
import com.avirajsharma.fundexplorer.ui.components.AddToWatchlistDialog
import com.avirajsharma.fundexplorer.ui.components.EmptyState
import com.avirajsharma.fundexplorer.ui.components.LoadingState
import com.avirajsharma.fundexplorer.ui.viewmodel.FundViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: FundViewModel,
    onFundClick: (Int) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val watchlistFolders by viewModel.watchlistFolders.collectAsState()

    var selectedFundForWatchlist by remember { mutableStateOf<FundSearchResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Funds") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TextField(
                value = query,
                onValueChange = { 
                    query = it
                    if (it.length > 2) viewModel.searchFunds(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search Mutual Funds...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )

            if (isLoading) {
                LoadingState()
            } else if (searchResults.isEmpty() && query.length > 2) {
                EmptyState(
                    icon = Icons.Default.SearchOff,
                    title = "No results found",
                    description = "Try searching for something else"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { fund ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onFundClick(fund.schemeCode) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = fund.schemeName,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                IconButton(onClick = { selectedFundForWatchlist = fund }) {
                                    Icon(Icons.Default.Add, contentDescription = "Add to Watchlist")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedFundForWatchlist != null) {
        AddToWatchlistDialog(
            folders = watchlistFolders,
            onDismiss = { selectedFundForWatchlist = null },
            onFolderSelected = { folderId ->
                selectedFundForWatchlist?.let { fund ->
                    viewModel.addFundToWatchlist(folderId, fund)
                }
                selectedFundForWatchlist = null
            },
            onCreateFolder = { name ->
                viewModel.createWatchlistFolder(name)
            }
        )
    }
}
