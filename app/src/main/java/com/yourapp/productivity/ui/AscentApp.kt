package com.yourapp.productivity.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.yourapp.productivity.ui.auth.AuthScreen
import com.yourapp.productivity.ui.profile.ProfileScreen
import com.yourapp.productivity.ui.settings.SettingsScreen
import com.yourapp.productivity.ui.statistics.StatisticsScreen
import com.yourapp.productivity.ui.tasks.AddEditTaskScreen
import com.yourapp.productivity.ui.tasks.TaskListScreen
import com.yourapp.productivity.ui.achievements.AchievementsScreen
import com.yourapp.productivity.ui.achievements.AddEditAchievementScreen
import com.yourapp.productivity.ui.components.LevelUpDialog
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch

@Composable
fun AscentApp(
    authViewModel: com.yourapp.productivity.ui.auth.AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val profileViewModel: com.yourapp.productivity.ui.profile.ProfileViewModel = hiltViewModel()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val currentLevel = profileUiState.userProgress?.currentLevel ?: 0

    var previousLevel by remember { mutableStateOf(currentLevel) }
    var showLevelUpForLevel by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(currentLevel) {
        if (previousLevel != 0 && currentLevel > previousLevel) {
            showLevelUpForLevel = currentLevel
        }
        if (currentLevel != 0) {
            previousLevel = currentLevel
        }
    }

    val showBars = currentRoute in listOf("tasks", "profile", "statistics", "settings")

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showBars,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") },
                    selected = currentRoute == "profile",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("profile") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("All Tasks") },
                    selected = currentRoute == "tasks",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("tasks") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("Statistics") },
                    selected = currentRoute == "statistics",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("statistics") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                    label = { Text("Achievements") },
                    selected = currentRoute == "achievements",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("achievements") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("settings") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(Modifier.weight(1f))
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                if (showBars) {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "Tasks") },
                            label = { Text("Tasks") },
                            selected = currentRoute == "tasks",
                            onClick = {
                                navController.navigate("tasks") {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile") },
                            selected = currentRoute in listOf("profile", "statistics", "settings"),
                            onClick = {
                                navController.navigate("profile") {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "loading",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("loading") {
                    LoadingScreen(
                        onLoadingComplete = { isAuthenticated ->
                            val route = if (isAuthenticated) "tasks" else "auth"
                            navController.navigate(route) {
                                popUpTo("loading") { inclusive = true }
                            }
                        }
                    )
                }
                // AuthScreen remains in the graph but is unreachable from startup
                composable("auth") {
                    AuthScreen(
                        onSignInSuccess = {
                            navController.navigate("tasks") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    )
                }
                composable("tasks") {
                    TaskListScreen(
                        onAddTaskClick = { navController.navigate("add_edit_task") },
                        onTaskClick = { taskId -> navController.navigate("add_edit_task?taskId=$taskId") },
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                }
                composable(
                    "add_edit_task?taskId={taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.StringType; nullable = true })
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getString("taskId")
                    AddEditTaskScreen(
                        onNavigateBack = { navController.popBackStack() },
                        taskId = taskId
                    )
                }
                composable("profile") {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    ProfileScreen(
                        onSignOut = {
                            scope.launch {
                                authViewModel.signOut()
                                try {
                                    CredentialManager.create(context)
                                        .clearCredentialState(ClearCredentialStateRequest())
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                navController.navigate("auth") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                }
                composable("statistics") {
                    StatisticsScreen(
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                }
                composable("settings") {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    SettingsScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onSignOut = {
                            scope.launch {
                                authViewModel.signOut()
                                try {
                                    CredentialManager.create(context)
                                        .clearCredentialState(ClearCredentialStateRequest())
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                navController.navigate("auth") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    )
                }
                composable("achievements") {
                    AchievementsScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onAddAchievementClick = { navController.navigate("add_edit_achievement") }
                    )
                }
                composable("add_edit_achievement") {
                    AddEditAchievementScreen(
                        onBackClick = { navController.popBackStack() },
                        onSaveSuccess = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    showLevelUpForLevel?.let { level ->
        LevelUpDialog(
            level = level,
            onDismiss = { showLevelUpForLevel = null }
        )
    }
}

