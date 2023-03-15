package com.kroncoders.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val text: String,
    val sentTime: Long,
    val senderId: Long,
    val conversationId: Long
)
