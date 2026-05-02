package com.avirajsharma.fundexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
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
import com.avirajsharma.fundexplorer.ui.screen.ViewAllScreen
import com.avirajsharma.fundexplorer.ui.screen.WatchlistFolderDetailScreen
import com.avirajsharma.fundexplorer.ui.screen.WatchlistScreen
import com.avirajsharma.fundexplorer.ui.viewmodel.FundViewModel
import com.avirajsharma.fundexplorer.ui.viewmodel.ThemeViewModel
import com.example.compose.FundExplorerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDarkModePreference by themeViewModel.isDarkMode.collectAsState()
            
            val useDarkTheme = isDarkModePreference ?: isSystemInDarkTheme()

            // Set dynamicColor to false to use your custom theme colors
            FundExplorerTheme(
                darkTheme = useDarkTheme,
                dynamicColor = false
            ) {
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
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Explore.route,
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        composable(Screen.Explore.route) {
                            ExploreScreen(
                                viewModel = viewModel,
                                themeViewModel = themeViewModel,
                                onFundClick = { schemeCode ->
                                    navController.navigate("detail/$schemeCode")
                                },
                                onViewAllClick = { category ->
                                    navController.navigate("view_all/$category")
                                },
                                onSearchClick = {
                                    navController.navigate(Screen.Search.route)
                                }
                            )
                        }
                        composable(Screen.Search.route) {
                            SearchScreen(viewModel) { schemeCode ->
                                navController.navigate("detail/$schemeCode")
                            }
                        }
                        composable(Screen.Watchlist.route) {
                            WatchlistScreen(
                                viewModel = viewModel,
                                onFolderClick = { folderId ->
                                    navController.navigate("watchlist/$folderId")
                                },
                                onExploreClick = {
                                    navController.navigate(Screen.Explore.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                        composable(
                            route = "view_all/{category}",
                            arguments = listOf(navArgument("category") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val category = backStackEntry.arguments?.getString("category") ?: ""
                            ViewAllScreen(
                                category = category,
                                viewModel = viewModel,
                                onFundClick = { schemeCode ->
                                    navController.navigate("detail/$schemeCode")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "watchlist/{folderId}",
                            arguments = listOf(navArgument("folderId") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
                            WatchlistFolderDetailScreen(
                                folderId, viewModel,
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
    data object Explore : Screen("explore", "Explore", Icons.Default.Home)
    data object Search : Screen("search", "Search", Icons.Default.Search)
    data object Watchlist : Screen("watchlist", "Watchlist", Icons.Default.BookmarkBorder)
}

val items = listOf(Screen.Explore, Screen.Watchlist)
