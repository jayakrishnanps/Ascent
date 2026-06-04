package com.yourapp.productivity.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.yourapp.productivity.ui.auth.AuthScreen
import com.yourapp.productivity.ui.profile.ProfileScreen
import com.yourapp.productivity.ui.tasks.AddEditTaskScreen
import com.yourapp.productivity.ui.tasks.TaskListScreen

@Composable
fun AscentApp() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser != null) "tasks" else "auth"

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf("tasks", "profile")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
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
                        selected = currentRoute == "profile",
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
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
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
                    onTaskClick = { taskId -> navController.navigate("add_edit_task?taskId=$taskId") }
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
                        navController.navigate("auth") {
                            popUpTo(0)
                        }
                    }
                )
            }
        }
    }
}
