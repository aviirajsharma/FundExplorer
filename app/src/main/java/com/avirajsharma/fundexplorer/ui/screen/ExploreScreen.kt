package com.avirajsharma.fundexplorer.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.avirajsharma.fundexplorer.data.model.FundSearchResult
import com.avirajsharma.fundexplorer.ui.components.AddToWatchlistDialog
import com.avirajsharma.fundexplorer.ui.components.ErrorState
import com.avirajsharma.fundexplorer.ui.components.LoadingState
import com.avirajsharma.fundexplorer.ui.viewmodel.FundViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: FundViewModel,
    onFundClick: (Int) -> Unit
) {
    val categoryFunds by viewModel.categoryFunds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val watchlistFolders by viewModel.watchlistFolders.collectAsState()

    var selectedFundForWatchlist by remember { mutableStateOf<FundSearchResult?>(null) }

    LaunchedEffect(Unit) {
        if (categoryFunds.isEmpty()) {
            viewModel.fetchExploreData()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explore Funds") }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (isLoading && categoryFunds.isEmpty()) {
                LoadingState()
            } else if (error != null && categoryFunds.isEmpty()) {
                ErrorState(
                    message = error ?: "Unknown error",
                    onRetry = { viewModel.fetchExploreData() })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    categoryFunds.forEach { (category, funds) ->
                        item {
                            CategorySection(
                                category = category,
                                funds = funds,
                                onFundClick = onFundClick,
                                onAddToWatchlist = { selectedFundForWatchlist = it }
                            )
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

@Composable
fun CategorySection(
    category: String,
    funds: List<FundSearchResult>,
    onFundClick: (Int) -> Unit,
    onAddToWatchlist: (FundSearchResult) -> Unit
) {
    Column {
        Text(
            text = category,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(funds) { fund ->
                FundCard(
                    fund = fund,
                    onClick = { onFundClick(fund.schemeCode) },
                    onAddClick = { onAddToWatchlist(fund) }
                )
            }
        }
    }
}

@Composable
fun FundCard(
    fund: FundSearchResult,
    onClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(120.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = fund.schemeName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    softWrap = true,
                    modifier = Modifier.weight(1f)
                )
            }
            IconButton(
                onClick = onAddClick,
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add to Watchlist",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
