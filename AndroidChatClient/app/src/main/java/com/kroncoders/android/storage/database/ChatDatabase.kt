package com.kroncoders.android.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kroncoders.android.storage.database.daos.ConversationDao
import com.kroncoders.android.storage.database.daos.MessageDao
import com.kroncoders.android.storage.database.daos.UserDao
import com.kroncoders.android.storage.database.entities.EntityConversation
import com.kroncoders.android.storage.database.entities.EntityMessage
import com.kroncoders.android.storage.database.entities.EntityUser
import com.kroncoders.android.storage.database.entities.UserConversationCrossRef

@Database(
    entities = [
        EntityUser::class,
        EntityMessage::class,
        EntityConversation::class,
        UserConversationCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messagesDao(): MessageDao
}