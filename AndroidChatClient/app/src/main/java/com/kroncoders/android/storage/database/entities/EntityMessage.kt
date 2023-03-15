package com.kroncoders.android.storage.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class EntityMessage(
    @PrimaryKey val id: String,
    val text: String,
    val sentTime: Long,
    val senderId: Long,
    val conversationId: Long,
    val isSent: Boolean,
)