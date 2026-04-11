package com.avirajsharma.fundexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.avirajsharma.fundexplorer.ui.screen.ExploreScreen
import com.avirajsharma.fundexplorer.ui.screen.FundDetailScreen
import com.avirajsharma.fundexplorer.ui.screen.SearchScreen
import com.avirajsharma.fundexplorer.ui.screen.WatchlistScreen
import com.avirajsharma.fundexplorer.ui.screen.WatchlistFolderDetailScreen
import com.avirajsharma.fundexplorer.ui.theme.FundExplorerTheme
import com.avirajsharma.fundexplorer.ui.viewmodel.FundViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FundExplorerTheme {
                val navController = rememberNavController()
                val viewModel: FundViewModel = hiltViewModel()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val currentRoute = currentDestination?.route

                val showBottomBar = items.any { it.route == currentRoute }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                items.forEach { screen ->
                                    NavigationBarItem(
                                        icon = { Icon(screen.icon, contentDescription = null) },
                                        label = { Text(screen.label) },
                                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    },
                    // Set contentWindowInsets to zero to avoid double padding for top insets
                    // as each screen handles its own TopAppBar and system insets.
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Explore.route,
                        // Only apply bottom padding for the NavigationBar if shown
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        composable(Screen.Explore.route) {
                            ExploreScreen(viewModel) { schemeCode ->
                                navController.navigate("detail/$schemeCode")
                            }
                        }
                        composable(Screen.Search.route) {
                            SearchScreen(viewModel) { schemeCode ->
                                navController.navigate("detail/$schemeCode")
                            }
                        }
                        composable(Screen.Watchlist.route) {
                            WatchlistScreen(viewModel) { folderId ->
                                navController.navigate("watchlist/$folderId")
                            }
                        }
                        composable(
                            route = "watchlist/{folderId}",
                            arguments = listOf(navArgument("folderId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
                            WatchlistFolderDetailScreen(folderId, viewModel, 
                                onFundClick = { schemeCode ->
                                    navController.navigate("detail/$schemeCode")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "detail/{schemeCode}",
                            arguments = listOf(navArgument("schemeCode") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val schemeCode = backStackEntry.arguments?.getInt("schemeCode") ?: 0
                            FundDetailScreen(schemeCode, viewModel) {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Explore : Screen("explore", "Explore", Icons.Default.Home)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Watchlist : Screen("watchlist", "Watchlist", Icons.Default.List)
}

val items = listOf(Screen.Explore, Screen.Search, Screen.Watchlist)
