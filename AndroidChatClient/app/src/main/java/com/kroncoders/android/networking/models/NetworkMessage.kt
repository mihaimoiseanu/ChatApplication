package com.kroncoders.android.networking.models

import kotlinx.serialization.Serializable

@Serializable
data class NetworkMessage(
    val id: String,
    val text: String,
    val sentTime: Long,
    val senderId: Long,
    val conversationId: Long
)
