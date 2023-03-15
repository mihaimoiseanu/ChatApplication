package com.kroncoders.android.storage.database.entities

import androidx.room.*

@Entity(
    primaryKeys = ["userId", "conversationId"],
    indices = [Index("conversationId"), Index("userId")]
)
data class UserConversationCrossRef(
    val userId: Long,
    val conversationId: Long,
)

data class ConversationWithUsers(
    @Embedded val entityConversation: EntityConversation,
    @Relation(
        parentColumn = "conversationId",
        entityColumn = "userId",
        associateBy = Junction(UserConversationCrossRef::class)
    )
    val entityUsers: List<EntityUser>
)

data class UserConversations(
    @Embedded val entityUser: EntityUser,
    @Relation(
        parentColumn = "userId",
        entityColumn = "conversationId",
        associateBy = Junction(UserConversationCrossRef::class)
    )
    val conversationEntities: List<EntityConversation>
)