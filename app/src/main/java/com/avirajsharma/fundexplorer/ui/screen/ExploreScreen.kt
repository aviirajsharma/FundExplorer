package com.avirajsharma.fundexplorer.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.avirajsharma.fundexplorer.data.model.FundSearchResult
import com.avirajsharma.fundexplorer.ui.viewmodel.FundViewModel

@Composable
fun ExploreScreen(
    viewModel: FundViewModel,
    onFundClick: (Int) -> Unit
) {
    val categoryFunds by viewModel.categoryFunds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchExploreData()
    }

    Scaffold(
        topBar = {
            Text(
                text = "Explore Funds",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        if (isLoading && categoryFunds.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                categoryFunds.forEach { (category, funds) ->
                    item {
                        CategorySection(
                            category = category,
                            funds = funds,
                            onFundClick = onFundClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySection(
    category: String,
    funds: List<FundSearchResult>,
    onFundClick: (Int) -> Unit
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
                FundCard(fund = fund, onClick = { onFundClick(fund.schemeCode) })
            }
        }
    }
}

@Composable
fun FundCard(
    fund: FundSearchResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fund.schemeName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                softWrap = true
            )
        }
    }
}
