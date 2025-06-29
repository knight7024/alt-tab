package com.example.module

import com.example.Phase
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.routing

internal fun Application.configureHTTP(phase: Phase) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.environment.log.error(
                "Unhandled exception on `${call.request.uri}`",
                cause,
            )
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
    install(CORS) {
        allowNonSimpleContentTypes = true
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.UserAgent)
        when (phase) {
            Phase.DEV -> {
                anyHost()
            }

            Phase.PROD -> {
                // TODO: allow only verified extension id
            }
        }
    }
    routing {
        swaggerUI(path = "openapi")
    }
}
