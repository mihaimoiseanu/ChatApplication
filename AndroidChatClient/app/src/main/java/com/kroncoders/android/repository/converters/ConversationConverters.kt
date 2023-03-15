package com.kroncoders.android.repository.converters

import com.kroncoders.android.networking.models.NetworkConversation
import com.kroncoders.android.repository.models.Conversation
import com.kroncoders.android.repository.models.User
import com.kroncoders.android.storage.database.entities.EntityConversation

fun NetworkConversation.toEntity(): EntityConversation {
    return EntityConversation(
        id = id!!,
        name = name,
        lastUpdateTime = lastUpdateTime
    )
}

fun Conversation.toNetworkModel(): NetworkConversation {
    return NetworkConversation(
        name = name,
        users = users.mapNotNull(User::id)
    )
}