package com.linkresolver.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resolution_history")
data class ResolutionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val originalUrl: String,

    val finalUrl: String?,

    val provider: String,

    val status: String,

    val redirectCount: Int,

    val timestamp: Long
)
