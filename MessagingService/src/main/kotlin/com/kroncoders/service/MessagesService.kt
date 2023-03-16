package com.kroncoders.service

import com.kroncoders.models.Message
import com.kroncoders.persistance.MessagesRepository

class MessagesService(
    private val messagesRepository: MessagesRepository
) {

    suspend fun getMessagesForConversation(conversationId: Long): List<Message> {
        return messagesRepository.readMessagesForConversation(conversationId)
    }

    suspend fun insertMessage(message: Message): Message {
        return messagesRepository.create(message).let { messagesRepository.read(it) }
    }
}