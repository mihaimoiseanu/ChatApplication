package com.kroncoders.android.ui.navigation.directions

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavOptionsBuilder
import com.kroncoders.android.ui.navigation.NavigationDirection

data class Call(
    val conversationId: Long,
    val calling: Boolean
) : NavigationDirection {

    override val arguments: Bundle = bundleOf(ConversationIdKey to conversationId, CallingKey to calling)
    override val navBuilder: NavOptionsBuilder.() -> Unit = {}

    companion object {
        private const val ConversationIdKey: String = "call:conversation:id:key"
        private const val CallingKey: String = "call:conversation:call:key"

        fun SavedStateHandle.getConversationIdForCall(): Long = this[ConversationIdKey] ?: throw IllegalArgumentException("ConversationId is missing")
        fun SavedStateHandle.getCalling(): Boolean = this[CallingKey] ?: false
    }
}