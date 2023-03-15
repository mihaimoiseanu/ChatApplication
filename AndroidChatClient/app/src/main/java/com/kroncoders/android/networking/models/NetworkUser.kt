package com.kroncoders.android.networking.models

import kotlinx.serialization.Serializable

@Serializable
data class NetworkUser(
    val id: Long? = null,
    val userName: String
)