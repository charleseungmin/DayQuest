package com.dayquest.app.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

@Composable
fun DayQuestNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val selectedRoute = navBackStackEntry?.destination?.route ?: DayQuestRoute.Today.route

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DayQuestRoute.entries.forEach { destination ->
                    val isSelected = navBackStackEntry
                        ?.destination
                        ?.hierarchy
                        ?.any { it.route == destination.route } == true
                    Button(
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        enabled = !isSelected
                    ) {
                        Text(destination.label)
                    }
                }
            }
        }

        Text(
            text = "현재 섹션: $selectedRoute",
            style = MaterialTheme.typography.bodySmall
        )

        NavHost(
            navController = navController,
            startDestination = DayQuestRoute.Today.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(DayQuestRoute.Today.route) { TodayScreen() }
            composable(DayQuestRoute.Manage.route) { TaskManageScreen() }
            composable(DayQuestRoute.History.route) { HistoryScreen() }
            composable(DayQuestRoute.Settings.route) { SettingsScreen() }
        }
    }
}
