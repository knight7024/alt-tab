package com.example.module

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

internal fun Application.configureHTTP() {
    install(StatusPages) {
        exception<Throwable> { call, t ->
            call.application.environment.log.error(
                "Unhandled exception on `${call.request.uri}`",
                t,
            )
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("MyCustomHeader")
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
    routing {
        swaggerUI(path = "openapi")
    }
}
