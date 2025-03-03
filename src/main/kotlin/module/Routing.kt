package com.example.module

import com.example.domain.TokenProvider
import com.example.domain.UserAuthenticationService
import com.example.domain.UserEmailRepository
import com.example.domain.UserRepository
import com.example.route.auth.authorization
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.time.Clock

internal fun Application.configureRouting(
    userEmailRepository: UserEmailRepository,
    userRepository: UserRepository,
    tokenProvider: TokenProvider,
    clock: Clock,
) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        authorization(
            userAuthenticationService =
                UserAuthenticationService(
                    googleEmailRepository = userEmailRepository,
                    userRepository = userRepository,
                    clock = clock,
                ),
            tokenProvider = tokenProvider,
        )
    }
}
