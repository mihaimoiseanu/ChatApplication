package com.kroncoders.android.storage.database.daos

import androidx.room.*
import com.kroncoders.android.storage.database.entities.ConversationWithUsers
import com.kroncoders.android.storage.database.entities.EntityConversation
import com.kroncoders.android.storage.database.entities.UserConversationCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Query("select count(*) from conversations where conversationId= :conversationId")
    fun countConversationWithId(conversationId: Long): Int

    @Transaction
    @Query("select * from conversations where conversationId= :conversationId")
    fun getConversation(conversationId: Long): Flow<ConversationWithUsers>

    @Transaction
    @Query("select * from conversations where conversationId= :conversationId")
    fun getUsersForConversation(conversationId: Long): List<ConversationWithUsers>

    @Query("select * from conversations")
    fun getConversations(): Flow<List<EntityConversation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConversation(vararg entityConversation: EntityConversation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserConversation(vararg userConversationCrossRef: UserConversationCrossRef)

    @Query("update conversations set lastUpdateTime= :lastUpdateTime where conversationId= :conversationId")
    fun updateLastUpdateTime(conversationId: Long, lastUpdateTime: Long)
}