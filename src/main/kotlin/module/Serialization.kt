package com.example.module

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json

internal fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                explicitNulls = false
                ignoreUnknownKeys = true
            },
        )
    }
}
