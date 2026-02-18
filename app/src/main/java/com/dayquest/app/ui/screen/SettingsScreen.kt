package com.dayquest.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dayquest.app.ui.component.ScreenSectionHeader

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.noticeMessage) {
        if (!state.noticeMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(state.noticeMessage!!)
            viewModel.consumeNotice()
        }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("데이터 초기화 확인") },
            text = { Text("tasks/daily_items/quests/streaks 로컬 데이터가 모두 초기화됩니다. 계속할까요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirmDialog = false
                        viewModel.resetData()
                    }
                ) {
                    Text("초기화 진행")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenSectionHeader(
                title = "Settings",
                subtitle = "알림/초기화 옵션"
            )

            if (state.isLoading) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("알림 사용", style = MaterialTheme.typography.titleMedium)
                        Text(if (state.notificationsEnabled) "07:00/21:00 알림 활성화" else "알림 비활성화")
                    }
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = viewModel::toggleNotifications,
                        enabled = !state.isLoading
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("오늘 데이터 동기화", style = MaterialTheme.typography.titleMedium)
                    Text("오늘 할 일/퀘스트/스트릭 상태를 수동으로 재생성 및 동기화합니다.")
                    Button(
                        onClick = viewModel::syncTodayData,
                        enabled = !state.isSyncingToday && !state.isLoading
                    ) {
                        Text(if (state.isSyncingToday) "동기화 중..." else "오늘 데이터 동기화")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("데이터 초기화", style = MaterialTheme.typography.titleMedium)
                    Text("앱의 로컬 DB(tasks/daily_items/quests/streaks)를 실제로 초기화합니다.")
                    Button(
                        onClick = { showResetConfirmDialog = true },
                        enabled = !state.isResetting && !state.isSyncingToday && !state.isLoading
                    ) {
                        Text(if (state.isResetting) "초기화 중..." else "초기화 실행")
                    }
                    if (state.resetDone) {
                        Text("초기화가 완료되었습니다.")
                    }
                    if (!state.errorMessage.isNullOrBlank()) {
                        Text(
                            text = state.errorMessage!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
