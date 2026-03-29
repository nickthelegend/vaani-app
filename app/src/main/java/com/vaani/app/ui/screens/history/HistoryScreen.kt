package com.vaani.app.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vaani.app.data.model.Task
import com.vaani.app.ui.components.TaskCard
import com.vaani.app.ui.theme.Background
import com.vaani.app.ui.theme.Primary
import com.vaani.app.ui.theme.PrimaryLight
import com.vaani.app.ui.theme.TextSecondary
import com.vaani.app.viewmodel.VaaniViewModel

@Composable
fun HistoryScreen(
    viewModel: VaaniViewModel,
    modifier: Modifier = Modifier
) {
    val taskHistory by viewModel.taskHistory.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .padding(top = 24.dp)
    ) {
        HistoryHeader(
            taskCount = taskHistory.size,
            onClearAll = { viewModel.clearAllTasks() }
        )

        AnimatedVisibility(
            visible = taskHistory.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            EmptyHistoryState()
        }

        AnimatedVisibility(
            visible = taskHistory.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = taskHistory,
                    key = { it.id }
                ) { task ->
                    TaskCard(
                        task = task,
                        onDelete = { viewModel.deleteTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryHeader(
    taskCount: Int,
    onClearAll: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Column {
            Text(
                text = "History",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (taskCount > 0) {
                Text(
                    text = "$taskCount tasks completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        if (taskCount > 0) {
            TextButton(
                onClick = onClearAll,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear all",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "Clear All",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Primary.copy(alpha = 0.3f),
                                PrimaryLight.copy(alpha = 0.1f),
                                Background
                            )
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No tasks yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Hold the button and speak.\nYour voice commands will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
