package com.avirajsharma.fundexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.avirajsharma.fundexplorer.data.api.MFApi
import com.avirajsharma.fundexplorer.data.repository.FundRepository
import com.avirajsharma.fundexplorer.ui.screen.ExploreScreen
import com.avirajsharma.fundexplorer.ui.screen.FundDetailScreen
import com.avirajsharma.fundexplorer.ui.screen.SearchScreen
import com.avirajsharma.fundexplorer.ui.theme.FundExplorerTheme
import com.avirajsharma.fundexplorer.ui.viewmodel.FundViewModel
import com.avirajsharma.fundexplorer.ui.viewmodel.FundViewModelFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val api = Retrofit.Builder()
            .baseUrl(MFApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(MFApi::class.java)

        val repository = FundRepository(api)
        val factory = FundViewModelFactory(repository)

        setContent {
            FundExplorerTheme {
                val navController = rememberNavController()
                val viewModel: FundViewModel = viewModel(factory = factory)

                Scaffold(
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination

                        if (currentDestination?.route != "detail/{schemeCode}") {
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
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Explore.route,
                        modifier = Modifier.padding(innerPadding)
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
}

val items = listOf(Screen.Explore, Screen.Search)
