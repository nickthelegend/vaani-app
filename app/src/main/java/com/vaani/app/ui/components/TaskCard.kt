package com.vaani.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaani.app.data.db.TaskEntity
import com.vaani.app.ui.theme.*

@Composable
fun TaskCard(
    task: TaskEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task Icon (Using a letter for now)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Primary, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (task.appName.isNullOrEmpty()) "A" else task.appName[0].toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1)) {
                Text(
                    text = task.translatedDescription,
                    style = Typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = "${task.appName} · ${formatTime(task.timestamp)}",
                    style = Typography.labelSmall,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            StatusChip(task.status == "DONE")
        }
    }
}

@Composable
fun StatusChip(success: Boolean) {
    Box(
        modifier = Modifier
            .background(
                if (success) Success.copy(alpha = 0.2f) else ErrorColor.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (success) "✓ Done" else "✗ Failed",
            color = if (success) Success else ErrorColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun formatTime(timestamp: Long): String {
    // Relative time formatting simplified
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "Yesterday"
    }
}
