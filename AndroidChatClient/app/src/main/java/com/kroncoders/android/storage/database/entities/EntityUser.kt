package com.kroncoders.android.storage.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class EntityUser(
    @PrimaryKey
    @ColumnInfo(name = "userId")
    val id: Long,
    val userName: String
)
