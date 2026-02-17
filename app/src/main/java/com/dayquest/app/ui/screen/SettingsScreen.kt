package com.dayquest.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dayquest.app.ui.component.ScreenSectionHeader
import com.dayquest.app.ui.logic.SettingsLogic
import com.dayquest.app.ui.model.SettingsUiState

@Composable
fun SettingsScreen() {
    var state by remember { mutableStateOf(SettingsUiState()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenSectionHeader(
            title = "Settings",
            subtitle = "알림/초기화 옵션"
        )

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
                    onCheckedChange = { state = SettingsLogic.toggleNotifications(state) }
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("데이터 초기화", style = MaterialTheme.typography.titleMedium)
                Text("학습용 샘플에서는 즉시 로컬 상태를 리셋했다고 가정합니다.")
                Button(onClick = { state = SettingsLogic.resetData(state) }) {
                    Text("초기화 실행")
                }
                if (state.resetDone) {
                    Text("초기화가 완료되었습니다.")
                }
            }
        }
    }
}
