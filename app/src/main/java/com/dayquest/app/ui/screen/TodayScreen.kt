package com.dayquest.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dayquest.app.ui.component.ScreenSectionHeader

@Composable
fun TodayScreen() {
    val todayChecklist = listOf(
        "아침 루틴 완료",
        "집중 작업 2회",
        "저녁 회고 작성"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenSectionHeader(
            title = "Today",
            subtitle = "오늘의 핵심 진행과 리듬 확인"
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("오늘의 진행", style = MaterialTheme.typography.titleMedium)
                Text("완료 1 / 3 · 연속 기록 5일")
                Text("다음 알림 21:00")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("오늘 체크리스트", style = MaterialTheme.typography.titleMedium)
                todayChecklist.forEachIndexed { index, item ->
                    Text("${index + 1}. $item")
                }
            }
        }
    }
}
