package com.kroncoders.android.repository

import com.kroncoders.android.networking.ChatRestApi
import com.kroncoders.android.networking.MessagingClient
import com.kroncoders.android.networking.models.NetworkConversation
import com.kroncoders.android.networking.models.NetworkMessage
import com.kroncoders.android.networking.models.NetworkUser
import com.kroncoders.android.repository.converters.toEntity
import com.kroncoders.android.repository.converters.toModel
import com.kroncoders.android.repository.converters.toNetworkModel
import com.kroncoders.android.repository.models.Conversation
import com.kroncoders.android.repository.models.Message
import com.kroncoders.android.repository.models.User
import com.kroncoders.android.storage.database.ChatDatabase
import com.kroncoders.android.storage.database.daos.ConversationDao
import com.kroncoders.android.storage.database.daos.MessageDao
import com.kroncoders.android.storage.database.daos.UserDao
import com.kroncoders.android.storage.database.entities.EntityUser
import com.kroncoders.android.storage.database.entities.UserConversationCrossRef
import com.kroncoders.android.storage.datastore.ChatDataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class OfflineFirstChatRepository(
    private val messagingClient: MessagingClient,
    private val userDao: UserDao,
    private val messagesDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val chatRestApi: ChatRestApi,
    private val chatDataStore: ChatDataStore,
    private val chatDatabase: ChatDatabase
) : ChatRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        listenToMessagesOnMessagingClient()
    }

    override suspend fun loginUser(user: User) {
        val userLoggedIn = chatRestApi.loginUser(user.toNetworkModel())
        chatDataStore.saveUserId(userId = userLoggedIn.id!!)
    }

    override suspend fun logout() {
        chatDataStore.saveUserId(-1L)
        chatDatabase.clearAllTables()
    }

    override suspend fun currentUserId(): Long = chatDataStore.userId.first()

    override fun connectToServer() {
        messagingClient.connectToWebSocket()
    }

    override fun getConversation(conversationId: Long): Flow<Conversation> {
        return conversationDao
            .getConversation(conversationId)
            .map { conversation ->
                Conversation(
                    id = conversation.entityConversation.id,
                    users = conversation.entityUsers.map(EntityUser::toModel),
                    name = conversation.entityConversation.name,
                    lastUpdateTime = conversation.entityConversation.lastUpdateTime,
                    lastMessage = ""
                )
            }
    }

    override fun getConversations(): Flow<List<Conversation>> {
        return conversationDao.getConversations().map { conversations ->
            conversations
                .map { conversation ->
                    val lastMessage = messagesDao.getLastMessageForConversation(conversation.id)
                    Conversation(
                        id = conversation.id,
                        users = emptyList(),
                        name = conversation.name,
                        lastUpdateTime = conversation.lastUpdateTime,
                        lastMessage = lastMessage?.text ?: ""
                    )
                }
                .sortedByDescending { it.lastUpdateTime }
        }
    }

    override fun getAllUsers(): Flow<List<User>> {
        return userDao
            .getAllUsers()
            .map { users -> users.map(EntityUser::toModel) }
    }

    override suspend fun createConversation(conversation: Conversation) {
        val currentUser = currentUserId()
        val conversationRequest = conversation.toNetworkModel().let {
            it.copy(users = it.users + currentUser)
        }
        val networkConversation = chatRestApi.createNewConversation(conversationRequest)
        syncConversation(networkConversation)
    }

    override suspend fun syncUsers() {
        val users = chatRestApi
            .getAllUsers()
            .map(NetworkUser::toEntity)
            .toTypedArray()
        userDao.insertUser(*users)
    }

    override fun getMessagesForConversation(conversationId: Long): Flow<List<Message>> {
        return messagesDao
            .getMessagesForConversation(conversationId)
            .map { messages -> messages.map { message -> message.toModel() } }
    }

    override suspend fun syncConversations() {
        coroutineScope {
            val userId = chatDataStore
                .userId
                .first()

            chatRestApi
                .getUserConversations(userId)
                .map { networkConversation -> async { syncConversation(networkConversation) } }
                .awaitAll()
        }
    }

    override suspend fun updateConversation(conversationId: Long, conversation: Conversation) {
        val networkRequest = conversation.toNetworkModel()
        val networkResponse = chatRestApi.updateConversation(conversationId, networkRequest)
        syncConversation(networkResponse)
    }

    override suspend fun syncConversation(conversationId: Long) = coroutineScope {
        val networkConversation = chatRestApi.getConversation(conversationId)
        syncConversation(networkConversation)
    }

    private suspend fun syncConversation(networkConversation: NetworkConversation) {
        coroutineScope {
            //Insert Conversation
            val conversationEntity = networkConversation.toEntity()
            conversationDao.insertConversation(conversationEntity)
            //Start sync messaging for conversation
            val syncMessagesDeferred = async { syncMessagesForConversation(conversationEntity.id) }
            //Start sync user for conversation
            val syncUserDeferred = async {
                val userEntities = networkConversation
                    .users
                    .map { userId ->
                        async(Dispatchers.IO) {
                            chatRestApi.getUser(userId).toEntity()
                        }
                    }
                    .awaitAll()
                    .toTypedArray()
                userDao.insertUser(*userEntities)

                val userConversationCrossRef = userEntities
                    .map { UserConversationCrossRef(it.id, conversationEntity.id) }
                    .toTypedArray()
                conversationDao.insertUserConversation(*userConversationCrossRef)
            }
            //Await for sync
            awaitAll(syncMessagesDeferred, syncUserDeferred)
        }
    }

    override suspend fun syncMessagesForConversation(conversationId: Long) {
        val messageEntities = chatRestApi
            .getMessagesForConversation(conversationId)
            .map(NetworkMessage::toEntity)
            .toTypedArray()
        if (messageEntities.isEmpty()) return
        messagesDao.insertMessage(*messageEntities)
        val lastUpdateTime = messageEntities.maxOf { it.sentTime }
        conversationDao.updateLastUpdateTime(conversationId, lastUpdateTime)
    }

    override suspend fun sendMessage(message: Message) {
        messagesDao.insertMessage(message.toEntity(false))
        messagingClient.sendTextMessage(message.toNetworkModel())
        conversationDao.updateLastUpdateTime(message.conversationId, message.sentTime)
    }

    private fun listenToMessagesOnMessagingClient() {
        messagingClient
            .messagesFlow
            .onEach(this::onNetworkMessageReceived)
            .launchIn(repositoryScope)
    }

    private suspend fun onNetworkMessageReceived(message: NetworkMessage) {
        checkIfUserExists(message.senderId)
        checkIfConversationExists(message.conversationId)
        messagesDao.insertMessage(message.toEntity())
    }

    private suspend fun checkIfUserExists(userId: Long) {
        val userCount = userDao.countUserWithId(userId)
        if (userCount == 1) return
        retrieveUser(userId)
    }

    private suspend fun retrieveUser(userId: Long) {
        val networkUser = chatRestApi.getUser(userId)
        userDao.insertUser(networkUser.toEntity())
    }

    private suspend fun checkIfConversationExists(conversationId: Long) {
        val conversationCount = conversationDao.countConversationWithId(conversationId)
        if (conversationCount == 1) return
        retrieveConversation(conversationId)
    }

    private suspend fun retrieveConversation(conversationId: Long) = coroutineScope {
        val networkConversation = chatRestApi.getConversation(conversationId)
        conversationDao.insertConversation(networkConversation.toEntity())
        networkConversation
            .users
            .map { userId -> async { checkIfUserExists(userId) } }
            .awaitAll()
    }

}

