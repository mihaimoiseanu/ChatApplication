package com.kroncoders.plugins

import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

val databaseModule = module {
    single<Database> {
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            user = "root",
            driver = "org.h2.Driver",
            password = ""
        )
    }
}