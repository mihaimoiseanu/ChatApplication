package com.kroncoders.models

import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    val id: Long? = null,
    val lastUpdateTime: Long = 0L,
    val name: String = "",
    val users: List<Long>
)
