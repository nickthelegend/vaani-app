package com.vaani.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

enum class ActionType {
    CLICK,
    TYPE,
    SCROLL_UP,
    SCROLL_DOWN,
    BACK,
    HOME,
    LAUNCH_APP,
    WAIT
}

data class AgentAction(
    val type: ActionType,
    val resourceId: String? = null,
    val text: String? = null,
    val appPackage: String? = null,
    val description: String
)

data class ExecutionResult(
    val success: Boolean,
    val completedActions: Int,
    val totalActions: Int,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ScreenNode(
    val resourceId: String?,
    val text: String?,
    val contentDescription: String?,
    val className: String?,
    val children: List<ScreenNode> = emptyList(),
    val bounds: Bounds? = null,
    val clickable: Boolean = false,
    val editable: Boolean = false
)

data class Bounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val description: String,
    val appName: String,
    val timestamp: Long,
    val status: String,
    val language: String,
    val errorMessage: String? = null
)

enum class TaskStatusEntity {
    DONE,
    FAILED,
    IN_PROGRESS
}
