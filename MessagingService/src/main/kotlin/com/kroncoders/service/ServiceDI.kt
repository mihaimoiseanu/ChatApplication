package com.kroncoders.service

import org.koin.dsl.module

val serviceModule = module {
    single { ConversationService(get(), get()) }
}