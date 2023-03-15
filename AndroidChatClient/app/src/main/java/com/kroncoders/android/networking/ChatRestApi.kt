package com.kroncoders.android.networking

import com.kroncoders.android.networking.models.NetworkConversation
import com.kroncoders.android.networking.models.NetworkMessage
import com.kroncoders.android.networking.models.NetworkUser
import retrofit2.http.*

interface ChatRestApi {
    @POST("user")
    suspend fun loginUser(@Body networkUser: NetworkUser): NetworkUser

    @GET("user")
    suspend fun getAllUsers(): List<NetworkUser>

    @GET("user/{id}")
    suspend fun getUser(@Path("id") id: Long): NetworkUser

    @GET("user/{id}/conversations")
    suspend fun getUserConversations(@Path("id") id: Long): List<NetworkConversation>

    @POST("conversations")
    suspend fun createNewConversation(@Body networkConversation: NetworkConversation): NetworkConversation

    @GET("conversations/{conversationId}")
    suspend fun getConversation(@Path("conversationId") conversationId: Long): NetworkConversation

    @GET("conversations/{conversationId}/messages")
    suspend fun getMessagesForConversation(@Path("conversationId") conversationId: Long): List<NetworkMessage>

    @PUT("conversations/{conversationId")
    suspend fun updateConversation(conversation: NetworkConversation): NetworkConversation
}