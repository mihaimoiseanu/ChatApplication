package com.kroncoders.android.networking.models

import kotlinx.serialization.Serializable

@Serializable
data class NetworkConversation(
    val id: Long? = null,
    val name: String,
    val lastUpdateTime: Long = 0L,
    val users: List<Long>
)
