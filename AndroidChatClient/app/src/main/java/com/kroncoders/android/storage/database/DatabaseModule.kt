package com.kroncoders.android.storage.database

import android.content.Context
import androidx.room.Room
import com.kroncoders.android.storage.database.daos.ConversationDao
import com.kroncoders.android.storage.database.daos.MessageDao
import com.kroncoders.android.storage.database.daos.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideChatDatabase(@ApplicationContext context: Context): ChatDatabase {
        return Room
            .databaseBuilder(context, ChatDatabase::class.java, "chat_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideUserDao(chatDatabase: ChatDatabase): UserDao = chatDatabase.userDao()

    @Singleton
    @Provides
    fun provideConversationDao(chatDatabase: ChatDatabase): ConversationDao =
        chatDatabase.conversationDao()

    @Singleton
    @Provides
    fun provideMessagesDao(chatDatabase: ChatDatabase): MessageDao = chatDatabase.messagesDao()
}