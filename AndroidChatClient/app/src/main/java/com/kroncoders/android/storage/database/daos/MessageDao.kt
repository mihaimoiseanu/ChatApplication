package com.kroncoders.android.storage.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kroncoders.android.storage.database.entities.EntityMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("select * from messages where conversationId= :conversationId order by sentTime desc")
    fun getMessagesForConversation(conversationId: Long): Flow<List<EntityMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(vararg entityMessage: EntityMessage)

    @Query("select * from messages where sentTime = (select max(sentTime) from messages where conversationId = :conversationId) and conversationId= :conversationId")
    fun getLastMessageForConversation(conversationId: Long): EntityMessage?
}