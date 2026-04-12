package com.avirajsharma.fundexplorer.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avirajsharma.fundexplorer.data.model.FundSearchResult
import com.avirajsharma.fundexplorer.ui.components.AddToWatchlistBottomSheet
import com.avirajsharma.fundexplorer.ui.components.ErrorState
import com.avirajsharma.fundexplorer.ui.components.LoadingState
import com.avirajsharma.fundexplorer.ui.viewmodel.FundViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAllScreen(
    category: String,
    viewModel: FundViewModel,
    onFundClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val categoryFunds by viewModel.categoryFunds.collectAsState()
    val funds = categoryFunds[category] ?: emptyList()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val watchlistFolders by viewModel.watchlistFolders.collectAsState()

    var selectedFundForWatchlist by remember { mutableStateOf<FundSearchResult?>(null) }

    LaunchedEffect(category) {
        if (funds.isEmpty()) {
            viewModel.fetchExploreData()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (isLoading && funds.isEmpty()) {
                LoadingState()
            } else if (error != null && funds.isEmpty()) {
                ErrorState(
                    message = error ?: "Unknown error",
                    onRetry = { viewModel.fetchExploreData() })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(funds) { fund ->
                        FundListItem(
                            fund = fund,
                            onClick = { onFundClick(fund.schemeCode) },
                            onAddClick = { selectedFundForWatchlist = fund }
                        )
                    }
                }
            }
        }
    }

    if (selectedFundForWatchlist != null) {
        AddToWatchlistBottomSheet(
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
fun FundListItem(
    fund: FundSearchResult,
    onClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFF)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCDDEB))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fund.schemeName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "NAV: ₹${(80..300).random()}.${(10..99).random()}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0052CC)
                    )
                )
            }
            IconButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "Add to watchlist",
                    tint = Color(0xFF0052CC)
                )
            }
        }
    }
}
