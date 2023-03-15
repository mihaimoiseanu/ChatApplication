package com.kroncoders.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long? = null,
    val userName: String
)