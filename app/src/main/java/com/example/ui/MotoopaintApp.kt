package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ui.screens.ColoursScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JobsScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.viewmodel.MotoopaintViewModel

enum class MotoopaintDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    COLOURS("colours", R.string.nav_colours, Icons.Filled.ColorLens, Icons.Outlined.ColorLens),
    STUDIO("studio", R.string.nav_studio, Icons.Filled.Palette, Icons.Outlined.Palette),
    JOBS("jobs", R.string.nav_jobs, Icons.Filled.Build, Icons.Outlined.Build),
    MORE("more", R.string.nav_more, Icons.Filled.Inventory2, Icons.Outlined.Inventory2)
}

@Composable
fun MotoopaintApp(viewModel: MotoopaintViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: MotoopaintDestination.HOME.route

    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()

    var studioSubTab by rememberSaveable { mutableIntStateOf(0) }
    var moreSubTab by rememberSaveable { mutableIntStateOf(0) }
    var triggerNewJob by remember { mutableStateOf(false) }
    var triggerNewStock by remember { mutableStateOf(false) }

    LaunchedEffect(uiMessage) {
        val msg = uiMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    fun navigateTopLevel(dest: MotoopaintDestination) {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        navController.navigate(dest.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar {
                        MotoopaintDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            val label = stringResource(dest.labelRes)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag("nav_${dest.route}")
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                when (currentRoute) {
                    MotoopaintDestination.HOME.route -> {
                        FloatingActionButton(
                            onClick = {
                                studioSubTab = 1 // CameraX Bay Scan
                                navigateTopLevel(MotoopaintDestination.STUDIO)
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.testTag("fab_home_camera")
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Scan Vehicle in Bay")
                        }
                    }
                    MotoopaintDestination.JOBS.route -> {
                        FloatingActionButton(
                            onClick = { triggerNewJob = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.testTag("fab_new_job")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "New Paint Job")
                        }
                    }
                    MotoopaintDestination.MORE.route -> {
                        if (moreSubTab == 0) {
                            FloatingActionButton(
                                onClick = { triggerNewStock = true },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.testTag("fab_new_stock")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Stock Item")
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail {
                        MotoopaintDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            val label = stringResource(dest.labelRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag("rail_${dest.route}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    NavHost(
                        navController = navController,
                        startDestination = MotoopaintDestination.HOME.route
                    ) {
                        composable(MotoopaintDestination.HOME.route) {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateColours = { q ->
                                    if (q.isNotBlank()) {
                                        viewModel.updateFilters { it.copy(query = q) }
                                    }
                                    navigateTopLevel(MotoopaintDestination.COLOURS)
                                },
                                onNavigateStudio = { subTab ->
                                    studioSubTab = subTab
                                    navigateTopLevel(MotoopaintDestination.STUDIO)
                                },
                                onNavigateJobs = { navigateTopLevel(MotoopaintDestination.JOBS) },
                                onNavigateMore = { subTab ->
                                    moreSubTab = subTab
                                    navigateTopLevel(MotoopaintDestination.MORE)
                                }
                            )
                        }
                        composable(MotoopaintDestination.COLOURS.route) {
                            ColoursScreen(
                                viewModel = viewModel,
                                onOpenInStudio = { hex, finish ->
                                    viewModel.sendColourToStudio(hex, finish)
                                    studioSubTab = 0
                                    navigateTopLevel(MotoopaintDestination.STUDIO)
                                },
                                onJobCreated = { navigateTopLevel(MotoopaintDestination.JOBS) }
                            )
                        }
                        composable(MotoopaintDestination.STUDIO.route) {
                            StudioScreen(
                                viewModel = viewModel,
                                selectedSubTab = studioSubTab,
                                onSelectSubTab = { studioSubTab = it },
                                onInspectOemCode = { code ->
                                    viewModel.updateFilters { it.copy(query = code) }
                                    navigateTopLevel(MotoopaintDestination.COLOURS)
                                }
                            )
                        }
                        composable(MotoopaintDestination.JOBS.route) {
                            JobsScreen(
                                viewModel = viewModel,
                                triggerNewJobDialog = triggerNewJob,
                                onDismissNewJobTrigger = { triggerNewJob = false }
                            )
                        }
                        composable(MotoopaintDestination.MORE.route) {
                            MoreScreen(
                                viewModel = viewModel,
                                selectedSubTab = moreSubTab,
                                onSelectSubTab = { moreSubTab = it },
                                triggerNewStockDialog = triggerNewStock,
                                onDismissNewStockTrigger = { triggerNewStock = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
