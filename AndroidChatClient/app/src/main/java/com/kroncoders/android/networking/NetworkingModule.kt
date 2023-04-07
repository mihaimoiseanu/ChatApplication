@file:OptIn(ExperimentalSerializationApi::class)

package com.kroncoders.android.networking

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kroncoders.android.networking.call.CallService
import com.kroncoders.android.networking.call.CallServiceImpl
import com.kroncoders.android.networking.messages.MessagesService
import com.kroncoders.android.networking.messages.MessagesServiceImpl
import com.kroncoders.android.networking.webrtc.session.WebRtcSessionManager
import com.kroncoders.android.storage.datastore.ChatDataStore
import com.kroncoders.android.ui.navigation.NavigationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

const val BaseUrl: String = "http://192.168.1.204:8080/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkingModule {

    @Singleton
    @Provides
    fun provideJsonSerializer(): Json {
        return Json {
            encodeDefaults = true
            isLenient = true
            allowSpecialFloatingPointValues = true
            allowStructuredMapKeys = true
            prettyPrint = true
            useArrayPolymorphism = false
            ignoreUnknownKeys = true
        }
    }

    @Singleton
    @Provides
    fun provideRetrofitClient(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BaseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor())
            .build()
    }

    @Singleton
    @Provides
    fun provideChatRestApi(retrofit: Retrofit): ChatRestApi {
        return retrofit.create(ChatRestApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMessagingService(webSocketMessagingService: WebSocketMessagingService, json: Json): MessagesService {
        return MessagesServiceImpl(webSocketMessagingService, json)
    }

    @Provides
    @Singleton
    fun provideCallService(
        webSocketMessagingService: WebSocketMessagingService,
        json: Json,
        chatDataStore: ChatDataStore,
        webRtcSessionManager: WebRtcSessionManager,
        navigationManager: NavigationManager
    ): CallService {
        return CallServiceImpl(chatDataStore, json, webSocketMessagingService, webRtcSessionManager, navigationManager)
    }
}