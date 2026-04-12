package com.avirajsharma.fundexplorer.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.avirajsharma.fundexplorer.ui.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: FundViewModel,
    themeViewModel: ThemeViewModel,
    onFundClick: (Int) -> Unit,
    onViewAllClick: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    val categoryFunds by viewModel.categoryFunds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val watchlistFolders by viewModel.watchlistFolders.collectAsState()
    val isDarkModePreference by themeViewModel.isDarkMode.collectAsState()
    
    val isDark = isDarkModePreference ?: isSystemInDarkTheme()

    var selectedFundForWatchlist by remember { mutableStateOf<FundSearchResult?>(null) }

    LaunchedEffect(Unit) {
        if (categoryFunds.isEmpty()) {
            viewModel.fetchExploreData()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "MF Explorer",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { themeViewModel.toggleTheme(!isDark) }) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme"
                                )
                            }
                            Text(
                                "View All",
                                modifier = Modifier
                                    .clickable { onViewAllClick("All Funds") }
                                    .padding(start = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        SearchBarPlaceholder(onClick = onSearchClick)
                    }

                    categoryFunds.forEach { (category, funds) ->
                        item {
                            CategorySection(
                                category = category,
                                funds = funds.take(4),
                                onFundClick = onFundClick,
                                onViewAllClick = { onViewAllClick(category) },
                                onAddClick = { selectedFundForWatchlist = it }
                            )
                        }
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
fun SearchBarPlaceholder(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Search funds...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategorySection(
    category: String,
    funds: List<FundSearchResult>,
    onFundClick: (Int) -> Unit,
    onViewAllClick: () -> Unit,
    onAddClick: (FundSearchResult) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            TextButton(onClick = onViewAllClick) {
                Text("View All >", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            funds.chunked(2).forEach { rowFunds ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowFunds.forEach { fund ->
                        FundCard(
                            fund = fund,
                            onClick = { onFundClick(fund.schemeCode) },
                            modifier = Modifier.weight(1f),
                            onAddClick = { onAddClick(fund) }
                        )
                    }
                    if (rowFunds.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun FundCard(
    fund: FundSearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAddClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .aspectRatio(1.2f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = fund.schemeName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "NAV",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "₹${(80..300).random()}.${(10..99).random()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (onAddClick != null) {
                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Add to watchlist",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
