package com.kroncoders.android.repository

import com.kroncoders.android.networking.ChatRestApi
import com.kroncoders.android.networking.MessagingClient
import com.kroncoders.android.storage.database.ChatDatabase
import com.kroncoders.android.storage.database.daos.ConversationDao
import com.kroncoders.android.storage.database.daos.MessageDao
import com.kroncoders.android.storage.database.daos.UserDao
import com.kroncoders.android.storage.datastore.ChatDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideChatRepository(
        messagingClient: MessagingClient,
        userDao: UserDao,
        messageDao: MessageDao,
        conversationDao: ConversationDao,
        chatRestApi: ChatRestApi,
        chatDataStore: ChatDataStore,
        chatDatabase: ChatDatabase
    ): ChatRepository = OfflineFirstChatRepository(
        messagingClient = messagingClient,
        userDao = userDao,
        messagesDao = messageDao,
        conversationDao = conversationDao,
        chatRestApi = chatRestApi,
        chatDataStore = chatDataStore,
        chatDatabase = chatDatabase
    )
}