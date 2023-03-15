package com.kroncoders.persistance

import org.koin.dsl.module

val repositoryModule = module {
    single { UserRepository(get()) }
    single { ConversationRepository(get()) }
    single { MessagesRepository(get()) }
    single { UsersConversationsRepository(get()) }
}