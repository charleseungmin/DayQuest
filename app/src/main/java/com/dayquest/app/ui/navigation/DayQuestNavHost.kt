package com.dayquest.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dayquest.app.ui.screen.HistoryScreen
import com.dayquest.app.ui.screen.SettingsScreen
import com.dayquest.app.ui.screen.TaskManageScreen
import com.dayquest.app.ui.screen.TodayScreen

private enum class DayQuestRoute(val route: String, val label: String) {
    Today("today", "Today"),
    Manage("manage", "Manage"),
    History("history", "History"),
    Settings("settings", "Settings")
}

private const val EDIT_TASK_ID_ARG = "editTaskId"
private const val MANAGE_ROUTE_WITH_OPTIONAL_EDIT = "manage?$EDIT_TASK_ID_ARG={$EDIT_TASK_ID_ARG}"

@Composable
fun DayQuestNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState().value

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                DayQuestRoute.entries.forEach { destination ->
                    val isSelected = navBackStackEntry
                        ?.destination
                        ?.hierarchy
                        ?.any { it.route == destination.route } == true

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Text(
                                text = destination.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        label = {
                            Text(destination.label)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DayQuestRoute.Today.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(DayQuestRoute.Today.route) {
                TodayScreen(
                    onGoToManage = { navController.navigate(DayQuestRoute.Manage.route) },
                    onEditTask = { taskId ->
                        navController.navigate("manage?$EDIT_TASK_ID_ARG=$taskId")
                    }
                )
            }
            composable(
                route = MANAGE_ROUTE_WITH_OPTIONAL_EDIT,
                arguments = listOf(navArgument(EDIT_TASK_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                TaskManageScreen(
                    initialEditTaskId = backStackEntry.arguments?.getString(EDIT_TASK_ID_ARG)
                )
            }
            composable(DayQuestRoute.History.route) { HistoryScreen() }
            composable(DayQuestRoute.Settings.route) { SettingsScreen() }
        }
    }
}
