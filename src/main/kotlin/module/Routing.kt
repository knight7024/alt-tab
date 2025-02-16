package com.example.module

import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/hello") {
            val email = call.queryParameters["email"]
            call.respondText("Hello $email!")
        }
    }
}
