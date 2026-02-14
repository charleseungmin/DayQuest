package com.dayquest.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

data class TodayTaskUi(
    val id: String,
    val title: String,
    val done: Boolean
)

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    TodayScreen()
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun TodayScreen() {
    val tasks = remember {
        mutableStateListOf(
            TodayTaskUi(id = "1", title = "아침 루틴 완료", done = false),
            TodayTaskUi(id = "2", title = "핵심 작업 1개 끝내기", done = false),
            TodayTaskUi(id = "3", title = "운동 20분", done = true)
        )
    }
    var quickAddText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuestCard(completed = tasks.count { it.done }, total = tasks.size)

        QuickAddSection(
            text = quickAddText,
            onTextChange = { quickAddText = it },
            onAdd = {
                val title = quickAddText.trim()
                if (title.isNotEmpty()) {
                    tasks.add(TodayTaskUi(id = System.currentTimeMillis().toString(), title = title, done = false))
                    quickAddText = ""
                }
            }
        )

        TaskList(
            items = tasks,
            onToggle = { id ->
                val idx = tasks.indexOfFirst { it.id == id }
                if (idx >= 0) {
                    val item = tasks[idx]
                    tasks[idx] = item.copy(done = !item.done)
                }
            }
        )
    }
}

@Composable
private fun QuestCard(completed: Int, total: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "오늘의 퀘스트", style = MaterialTheme.typography.titleMedium)
            Text(text = "진행도: $completed / $total")
            Text(text = if (completed >= total && total > 0) "퀘스트 달성! ⚒️" else "하나씩 정복하자")
        }
    }
}

@Composable
private fun QuickAddSection(
    text: String,
    onTextChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = text,
            onValueChange = onTextChange,
            label = { Text("빠른 추가") },
            singleLine = true
        )
        Button(onClick = onAdd) {
            Text("추가")
        }
    }
}

@Composable
private fun TaskList(
    items: List<TodayTaskUi>,
    onToggle: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        LazyColumn(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(items, key = { it.id }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = item.done, onCheckedChange = { onToggle(item.id) })
                    Text(text = item.title, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
