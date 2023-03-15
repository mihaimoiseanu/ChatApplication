package com.kroncoders.android.ui.navigation.directions

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavOptionsBuilder
import com.kroncoders.android.ui.navigation.NavigationDirection

class Chat(conversationId: Long) : NavigationDirection {
    override val arguments: Bundle = bundleOf(ConversationIdKey to conversationId)
    override val navBuilder: NavOptionsBuilder.() -> Unit = {}

    companion object {
        fun SavedStateHandle.getConversationId(): Long =
            this[ConversationIdKey] ?: throw IllegalArgumentException("Conversation Key is missing")

        private const val ConversationIdKey = "conversation_id_key"
    }
}