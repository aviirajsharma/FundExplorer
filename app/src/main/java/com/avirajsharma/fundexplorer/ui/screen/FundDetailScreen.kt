package com.avirajsharma.fundexplorer.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avirajsharma.fundexplorer.data.model.FundSearchResult
import com.avirajsharma.fundexplorer.ui.components.AddToWatchlistBottomSheet
import com.avirajsharma.fundexplorer.ui.components.ErrorState
import com.avirajsharma.fundexplorer.ui.components.LineChart
import com.avirajsharma.fundexplorer.ui.components.LoadingState
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
    val isInWatchlist by viewModel.isFundInWatchlist(schemeCode).collectAsState(initial = false)

    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(schemeCode) {
        viewModel.fetchFundDetails(schemeCode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analysis", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(
                            imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Watchlist",
                            tint = if (isInWatchlist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (isLoading) {
                LoadingState()
            } else if (error != null) {
                ErrorState(
                    message = error ?: "Failed to load details",
                    onRetry = { viewModel.fetchFundDetails(schemeCode) })
            } else {
                fundDetails?.let { details ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = details.meta.schemeName.uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 28.sp
                        )
                        Text(
                            text = "Category: ${details.meta.schemeCategory}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "NAV ",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${details.navHistory.firstOrNull()?.nav ?: "0.00"}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            // Placeholder for trend
                            Text(
                                text = " ↑ 1.22%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.padding(bottom = 4.dp, start = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            LineChart(
                                navHistory = details.navHistory.take(30), // Last 30 days for performance
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "6M",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(24.dp))
                            Text("1Y", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(24.dp))
                            Text("ALL", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        Text(
                            text = "${details.meta.fundHouse} is an open-ended equity scheme investing in ${details.meta.schemeCategory}. The investment objective is to provide investors with opportunities for long-term capital appreciation.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DetailInfoItem("Type", details.meta.schemeType)
                            DetailInfoItem(
                                "Size",
                                "₹2356 Cr"
                            ) // Static for now as API doesn't provide it
                            DetailInfoItem("NAV", "₹${details.navHistory.firstOrNull()?.nav}")
                        }
                    }
                }
            }
        }

        if (showBottomSheet) {
            AddToWatchlistBottomSheet(
                folders = watchlistFolders,
                onDismiss = { showBottomSheet = false },
                onFolderSelected = { folderId ->
                    fundDetails?.let { details ->
                        viewModel.addFundToWatchlist(
                            folderId,
                            FundSearchResult(schemeCode, details.meta.schemeName)
                        )
                    }
                    showBottomSheet = false
                },
                onCreateFolder = { name ->
                    viewModel.createWatchlistFolder(name)
                }
            )
        }
    }
}

@Composable
fun DetailInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
