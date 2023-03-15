package com.kroncoders.android.repository.converters

import com.kroncoders.android.networking.models.NetworkMessage
import com.kroncoders.android.repository.models.Message
import com.kroncoders.android.storage.database.entities.EntityMessage

fun Message.toNetworkModel(): NetworkMessage {
    return NetworkMessage(
        id = id,
        text = text,
        sentTime = sentTime,
        senderId = userId,
        conversationId = conversationId
    )
}

fun Message.toEntity(isSent: Boolean): EntityMessage {
    return EntityMessage(
        id = id,
        text = text,
        sentTime = sentTime,
        senderId = userId,
        conversationId = conversationId,
        isSent = isSent
    )
}

fun NetworkMessage.toEntity(): EntityMessage {
    return EntityMessage(
        id = id,
        text = text,
        sentTime = sentTime,
        conversationId = conversationId,
        senderId = senderId,
        isSent = true
    )
}

fun EntityMessage.toModel(): Message {
    return Message(
        id = id,
        text = text,
        sentTime = sentTime,
        userId = senderId,
        conversationId = conversationId,
        isSent = isSent
    )
}