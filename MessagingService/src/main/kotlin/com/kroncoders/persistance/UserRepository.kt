package com.kroncoders.persistance

import com.kroncoders.models.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository(private val database: Database) {

    object Users : Table() {
        val id = long("id").autoIncrement()
        val userName = varchar("user_name", length = 50).uniqueIndex()

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun create(user: User): Long = dbQuery {
        Users.insert {
            it[userName] = user.userName
        }[Users.id]
    }

    suspend fun readAll(): List<User> = dbQuery {
        Users.selectAll().map {
            User(it[Users.id], it[Users.userName])
        }
    }

    suspend fun read(id: Long): User? = dbQuery {
        Users
            .select { Users.id eq id }
            .map { User(it[Users.id], it[Users.userName]) }
            .singleOrNull()
    }

    suspend fun read(userName: String): User? = dbQuery {
        Users
            .select { Users.userName eq userName }
            .map { User(it[Users.id], it[Users.userName]) }
            .singleOrNull()
    }

    suspend fun readUsersForConversation(conversationId: Long): List<User> = dbQuery {
        Users.join(
            otherTable = UsersConversationsRepository.UsersConversations,
            onColumn = Users.id,
            otherColumn = UsersConversationsRepository.UsersConversations.user,
            additionalConstraint = { Users.id eq UsersConversationsRepository.UsersConversations.user },
            joinType = JoinType.RIGHT
        )
            .slice(Users.id, Users.userName)
            .select { UsersConversationsRepository.UsersConversations.conversation eq conversationId }
            .map {
                User(it[Users.id], it[Users.userName])
            }
    }

    suspend fun update(id: Long, user: User) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[userName] = user.userName
            }
        }
    }

    suspend fun delete(id: Long) {
        dbQuery { Users.deleteWhere { Users.id eq id } }
    }
}