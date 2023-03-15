package com.kroncoders.android.storage.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class EntityConversation(
    @PrimaryKey
    @ColumnInfo(name = "conversationId")
    val id: Long,
    val name: String,
    val lastUpdateTime: Long
)
