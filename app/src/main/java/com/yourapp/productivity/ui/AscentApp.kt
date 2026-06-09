package com.yourapp.productivity.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yourapp.productivity.ui.auth.AuthScreen
import com.yourapp.productivity.ui.profile.ProfileScreen
import com.yourapp.productivity.ui.settings.SettingsScreen
import com.yourapp.productivity.ui.statistics.StatisticsScreen
import com.yourapp.productivity.ui.tasks.AddEditTaskScreen
import com.yourapp.productivity.ui.tasks.TaskListScreen
import kotlinx.coroutines.launch

@Composable
fun AscentApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("Achievements") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
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
                    ProfileScreen(
                        onSignOut = {
                            // --- GOOGLE SIGN-IN DISABLED ---
                            // Firebase sign-out and CredentialManager clearing are bypassed.
                            // We just pop back to tasks or show a mock action.
                            /*
                            scope.launch {
                                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                try {
                                    androidx.credentials.CredentialManager.create(context).clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                navController.navigate("auth") {
                                    popUpTo(0)
                                }
                            }
                            */
                            navController.navigate("tasks") {
                                popUpTo(0)
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
                    SettingsScreen(
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                }
            }
        }
    }
}

