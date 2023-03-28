package com.kroncoders.android.networking.webrtc.di

import android.content.Context
import com.kroncoders.android.networking.webrtc.peer.StreamPeerConnectionFactory
import com.kroncoders.android.networking.webrtc.session.WebRtcSessionManager
import com.kroncoders.android.networking.webrtc.session.WebRtcSessionManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebRTCModule {

    @Provides
    @Singleton
    fun provideWebRtcSessionManager(
        @ApplicationContext context: Context,
        streamPeerConnectionFactory: StreamPeerConnectionFactory
    ): WebRtcSessionManager {
        return WebRtcSessionManagerImpl(
            context = context,
            peerConnectionFactory = streamPeerConnectionFactory
        )
    }
}