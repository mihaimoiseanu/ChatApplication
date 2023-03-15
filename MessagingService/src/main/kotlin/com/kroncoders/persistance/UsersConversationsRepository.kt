package com.kroncoders.persistance

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.transactions.transaction

class UsersConversationsRepository(private val database: Database) {

    object UsersConversations : Table() {
        val user = reference("user_id", UserRepository.Users.id, onDelete = ReferenceOption.CASCADE)
        val conversation =
            reference("conversation_id", ConversationRepository.Conversations.id, onDelete = ReferenceOption.CASCADE)

        override val primaryKey: PrimaryKey = PrimaryKey(arrayOf(user, conversation))
    }

    init {
        transaction(database) {
            SchemaUtils.create(UsersConversations)
        }
    }

    suspend fun create(userId: Long, conversationId: Long) {
        dbQuery {
            UsersConversations.insert {
                it[user] = userId
                it[conversation] = conversationId
            }
        }
    }

    suspend fun readUsersForConversation(conversationId: Long): List<Long> = dbQuery {
        UsersConversations
            .select { UsersConversations.conversation eq conversationId }
            .map { it[UsersConversations.user] }
    }

    suspend fun deleteUserFromConversation(conversationId: Long, users: List<Long>) = dbQuery {
        UsersConversations
            .deleteWhere { (user notInList users) and (conversation eq conversationId) }
    }
}