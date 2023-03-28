package com.kroncoders.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.getstream.log.StreamLog
import io.getstream.log.kotlin.KotlinStreamLogger

@HiltAndroidApp
class ChatApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        StreamLog.install(KotlinStreamLogger())
    }
}