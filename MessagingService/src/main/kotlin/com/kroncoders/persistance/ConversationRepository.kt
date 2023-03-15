package com.kroncoders.persistance

import com.kroncoders.models.Conversation
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ConversationRepository(private val database: Database) {

    object Conversations : Table() {
        val id = long("id").autoIncrement()
        val lastUpdateTime = long("last_update_time")
        val name = varchar("name", length = 250)

        override val primaryKey: PrimaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Conversations)
        }
    }

    suspend fun create(name: String): Long = dbQuery {
        Conversations.insert {
            it[lastUpdateTime] = System.currentTimeMillis()
            it[Conversations.name] = name
        }[Conversations.id]
    }

    suspend fun read(id: Long): Conversation = dbQuery {
        Conversations.select { Conversations.id eq id }
            .map {
                Conversation(
                    it[Conversations.id],
                    it[Conversations.lastUpdateTime],
                    it[Conversations.name],
                    emptyList()
                )
            }
            .single()
    }

    suspend fun getUserConversations(userId: Long): List<Conversation> = dbQuery {
        Conversations.join(
            UsersConversationsRepository.UsersConversations,
            joinType = JoinType.RIGHT,
            onColumn = Conversations.id,
            otherColumn = UsersConversationsRepository.UsersConversations.conversation,
        )
            .slice(listOf(Conversations.id, Conversations.name, Conversations.lastUpdateTime))
            .select { UsersConversationsRepository.UsersConversations.user eq userId }
            .map {
                Conversation(
                    it[Conversations.id],
                    it[Conversations.lastUpdateTime],
                    it[Conversations.name],
                    emptyList()
                )
            }
    }

    suspend fun update(id: Long, conversation: Conversation) {
        dbQuery {
            Conversations.update({ Conversations.id eq id }) {
                it[lastUpdateTime] = conversation.lastUpdateTime
                it[name] = conversation.name
            }
        }
    }

    suspend fun delete(id: Long) {
        dbQuery {
            Conversations.deleteWhere { Conversations.id eq id }
        }
    }
}