package com.kroncoders.android.repository.models

data class Message(
    val id: String,
    val text: String,
    val sentTime: Long,
    val userId: Long,
    val conversationId: Long,
    val isSent: Boolean = false
)
