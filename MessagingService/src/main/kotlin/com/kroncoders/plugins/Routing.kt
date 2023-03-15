package com.kroncoders.plugins

import com.kroncoders.routing.conversationRouting
import com.kroncoders.routing.messagesRouting
import com.kroncoders.routing.userRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.exceptions.ExposedSQLException

fun Application.configureRouting() {
    install(StatusPages) {
        exception<ExposedSQLException> { call, cause ->
            call.respondText(text = "Bad Request", status = HttpStatusCode.BadRequest)
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respondText(text = cause.message ?: "MissingInfo", status = HttpStatusCode.BadRequest)
        }
        exception<IllegalStateException> { call, cause ->
            call.respondText(status = HttpStatusCode.NotFound, text = cause.message ?: "Not Found")
        }
    }
    routing {
        userRouting()
        conversationRouting()
        messagesRouting()
    }
}
