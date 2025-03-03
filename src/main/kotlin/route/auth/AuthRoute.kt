package com.example.route.auth

import com.example.domain.TokenProvider
import com.example.domain.UserAuthenticationService
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

fun Routing.authorization(
    userAuthenticationService: UserAuthenticationService,
    tokenProvider: TokenProvider,
) {
    route("/oauth") {
        authenticate("auth-oauth-google") {
            get("/google") {
                val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()!!
                val user = userAuthenticationService.byGoogleOAuth(principal.accessToken)

                val accessToken = tokenProvider.issueAccessToken(user)
                val refreshToken = tokenProvider.issueRefreshToken(user)

                call.respond(TokenResult(accessToken.value, refreshToken.value))
            }
        }
    }
}

@Serializable
private data class TokenResult(
    val accessToken: String,
    val refreshToken: String,
)

fun Routing.authentication() {
    authenticate("auth-jwt") {
    }
}
