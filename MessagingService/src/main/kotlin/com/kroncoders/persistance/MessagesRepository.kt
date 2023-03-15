package com.kroncoders.persistance

import com.kroncoders.models.Message
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class MessagesRepository(private val database: Database) {

    object Messages : Table() {
        val id = varchar("id", length = 50)
        val text = varchar("text", length = 250)
        val sentTime = long("sent_time")
        val sender = reference("sender_id", UserRepository.Users.id)
        val conversation = reference("conversation_id", ConversationRepository.Conversations.id)

        override val primaryKey: PrimaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Messages)
        }
    }

    suspend fun create(message: Message): String = dbQuery {
        Messages.insert {
            it[id] = message.id
            it[text] = message.text
            it[sentTime] = message.sentTime
            it[sender] = message.senderId
            it[conversation] = message.conversationId
        }[Messages.id]
    }

    suspend fun read(messageId: String): Message = dbQuery {
        Messages.select { Messages.id eq messageId }
            .map {
                Message(
                    id = it[Messages.id],
                    text = it[Messages.text],
                    sentTime = it[Messages.sentTime],
                    senderId = it[Messages.sender],
                    conversationId = it[Messages.conversation]
                )
            }.first()
    }

    suspend fun readMessagesForConversation(conversationId: Long): List<Message> = dbQuery {
        Messages.select { Messages.conversation eq conversationId }
            .orderBy(Messages.sentTime, order = SortOrder.DESC)
            .map {
                Message(
                    id = it[Messages.id],
                    text = it[Messages.text],
                    sentTime = it[Messages.sentTime],
                    senderId = it[Messages.sender],
                    conversationId = it[Messages.conversation]
                )
            }
    }

}