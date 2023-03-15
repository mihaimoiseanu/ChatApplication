package com.kroncoders.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.ext.inject

val serializationModule = module {
    single {
        Json {
            encodeDefaults = true
            isLenient = true
            allowSpecialFloatingPointValues = true
            allowStructuredMapKeys = true
            prettyPrint = true
            useArrayPolymorphism = false
            ignoreUnknownKeys = true
        }
    }
}

fun Application.configureSerialization() {
    val json by inject<Json>()
    install(ContentNegotiation) {
        json(json)
    }
}
