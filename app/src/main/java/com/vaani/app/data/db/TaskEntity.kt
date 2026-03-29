package com.vaani.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val translatedDescription: String,
    val appPackage: String? = null,
    val appName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // DONE / FAILED / IN_PROGRESS
    val language: String,
    val actionsJson: String? = null,
    val errorMessage: String? = null,
    val durationMs: Long = 0
)
