package com.example.module

import com.example.domain.token.RefreshTokenRepository
import com.example.domain.token.TokenProvider
import com.example.domain.token.TokenValidator
import com.example.domain.user.UserAuthorizationService
import com.example.route.auth.authorization
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

internal fun Application.configureRouting(
    userAuthorizationService: UserAuthorizationService,
    tokenProvider: TokenProvider,
    tokenValidator: TokenValidator,
    refreshTokenRepository: RefreshTokenRepository,
) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        authorization(
            userAuthorizationService = userAuthorizationService,
            tokenProvider = tokenProvider,
            tokenValidator = tokenValidator,
            refreshTokenRepository = refreshTokenRepository,
        )
    }
}
