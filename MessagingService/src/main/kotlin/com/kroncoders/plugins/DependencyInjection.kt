package com.kroncoders.plugins

import com.kroncoders.messaging.sessionModule
import com.kroncoders.persistance.repositoryModule
import com.kroncoders.service.serviceModule
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin

fun Application.configureDependencyInjection() {
    install(Koin) {
        modules(databaseModule + repositoryModule + sessionModule + serializationModule + serviceModule)
    }
}