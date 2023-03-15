package com.kroncoders.android.repository.models

data class Conversation(
    val id: Long = -1L,
    val users: List<User>,
    val name: String,
    val lastUpdateTime: Long = -1L,
    val lastMessage: String = ""
)
