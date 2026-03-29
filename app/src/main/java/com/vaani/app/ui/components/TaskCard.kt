package com.vaani.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vaani.app.data.model.Task
import com.vaani.app.data.model.TaskStatus
import com.vaani.app.ui.theme.CardBorder
import com.vaani.app.ui.theme.Error
import com.vaani.app.ui.theme.Primary
import com.vaani.app.ui.theme.Success
import com.vaani.app.ui.theme.Surface
import com.vaani.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskCard(
    task: Task,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> Error.copy(alpha = 0.8f)
                    else -> Color.Transparent
                },
                label = "swipeColor"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = Color.White
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false,
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconPlaceholder(appName = task.appName)

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = task.appName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary
                        )

                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        }

                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Time",
                            modifier = Modifier.size(12.dp),
                            tint = TextSecondary
                        )

                        Text(
                            text = formatTimestamp(task.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                StatusChip(status = task.status)
            }
        }
    }
}

@Composable
fun AppIconPlaceholder(
    appName: String,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFF25D366),
        Color(0xFF4285F4),
        Color(0xFFEA4335),
        Color(0xFFFBBC05),
        Color(0xFF9333EA),
        Color(0xFF06B6D4)
    )

    val colorIndex = appName.hashCode().let { kotlin.math.abs(it) % colors.size }

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(colors[colorIndex].copy(alpha = 0.2f))
            .border(1.dp, colors[colorIndex].copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = appName.first().toString(),
            style = MaterialTheme.typography.titleMedium,
            color = colors[colorIndex]
        )
    }
}

@Composable
fun StatusChip(
    status: TaskStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, icon, text) = when (status) {
        TaskStatus.DONE -> Quadruple(
            Success.copy(alpha = 0.15f),
            Success,
            Icons.Default.Check,
            "Done"
        )
        TaskStatus.FAILED -> Quadruple(
            Error.copy(alpha = 0.15f),
            Error,
            Icons.Default.Close,
            "Failed"
        )
        TaskStatus.IN_PROGRESS -> Quadruple(
            Primary.copy(alpha = 0.15f),
            Primary,
            Icons.Default.Schedule,
            "In Progress"
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = textColor
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
