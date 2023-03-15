package com.kroncoders.android.repository.converters

import com.kroncoders.android.networking.models.NetworkUser
import com.kroncoders.android.repository.models.User
import com.kroncoders.android.storage.database.entities.EntityUser

fun NetworkUser.toEntity(): EntityUser {
    return EntityUser(
        id = id!!,
        userName = userName
    )
}

fun User.toNetworkModel(): NetworkUser {
    return NetworkUser(
        id = id,
        userName = userName
    )
}

fun EntityUser.toModel(): User {
    return User(
        id = id,
        userName = userName
    )
}