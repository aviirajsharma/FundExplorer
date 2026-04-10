package com.avirajsharma.fundexplorer.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            fundDetails?.let { details ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
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
}
