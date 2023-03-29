package com.kroncoders.android.networking.messages

import com.kroncoders.android.networking.models.NetworkMessage
import kotlinx.coroutines.flow.SharedFlow

interface MessagesService {

    val messagesStream: SharedFlow<NetworkMessage>

    fun sendTextMessage(networkMessage: NetworkMessage)
}