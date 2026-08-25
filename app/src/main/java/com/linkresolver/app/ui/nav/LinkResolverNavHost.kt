package com.linkresolver.app.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.linkresolver.app.data.repository.ResolverRepository
import com.linkresolver.app.domain.model.ResolverStatus
import com.linkresolver.app.ui.batch.BatchScreen
import com.linkresolver.app.ui.batch.BatchViewModel
import com.linkresolver.app.ui.batch.BatchViewModelFactory
import com.linkresolver.app.ui.history.HistoryScreen
import com.linkresolver.app.ui.home.HomeScreen
import com.linkresolver.app.ui.home.ResolverUiState
import com.linkresolver.app.ui.home.ResolverViewModel
import com.linkresolver.app.ui.home.ResolverViewModelFactory
import com.linkresolver.app.ui.result.ResultScreen
import com.linkresolver.app.ui.settings.SettingsScreen
import androidx.lifecycle.viewmodel.compose.viewModel

sealed class Destination(val route: String, val label: String) {
    data object Home : Destination("home", "Home")
    data object History : Destination("history", "History")
    data object Settings : Destination("settings", "Settings")
    data object Result : Destination("result", "Result")
    data object Batch : Destination("batch", "Batch")
}

private val bottomNavDestinations = listOf(Destination.Home, Destination.History, Destination.Settings)

@Composable
fun LinkResolverNavHost(
    repository: ResolverRepository,
    sharedTextToResolve: String?
) {
    val navController = rememberNavController()
    val resolverViewModel: ResolverViewModel = viewModel(factory = ResolverViewModelFactory(repository))
    val batchViewModel: BatchViewModel = viewModel(factory = BatchViewModelFactory(repository))

    // If the app was opened via Android's share sheet, prefill the input.
    androidx.compose.runtime.LaunchedEffect(sharedTextToResolve) {
        if (!sharedTextToResolve.isNullOrBlank()) {
            resolverViewModel.setInputFromShare(sharedTextToResolve)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavDestinations.map { it.route }) {
                NavigationBar {
                    bottomNavDestinations.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    when (dest) {
                                        Destination.Home -> Icons.Filled.Home
                                        Destination.History -> Icons.Filled.History
                                        Destination.Settings -> Icons.Filled.Settings
                                        else -> Icons.Filled.Home
                                    },
                                    contentDescription = dest.label
                                )
                            },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destination.Home.route) {
                HomeScreen(
                    viewModel = resolverViewModel,
                    onOpenResult = { navController.navigate(Destination.Result.route) },
                    onNavigateToBatch = { navController.navigate(Destination.Batch.route) }
                )
            }
            composable(Destination.Result.route) {
                val uiState by resolverViewModel.state.collectAsStateWithLifecycle()
                val result = when (val s = uiState) {
                    is ResolverUiState.Success -> s.result
                    is ResolverUiState.Error -> s.result
                    else -> null
                }
                if (result != null) {
                    ResultScreen(
                        result = result,
                        onBack = {
                            resolverViewModel.resetToIdle()
                            navController.popBackStack()
                        },
                        onResolveAgain = {
                            resolverViewModel.resetToIdle()
                            navController.popBackStack()
                        }
                    )
                }
            }
            composable(Destination.History.route) {
                HistoryScreen(viewModel = resolverViewModel)
            }
            composable(Destination.Settings.route) {
                SettingsScreen(viewModel = resolverViewModel)
            }
            composable(Destination.Batch.route) {
                BatchScreen(viewModel = batchViewModel)
            }
        }
    }
}

