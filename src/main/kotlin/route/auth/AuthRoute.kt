package com.example.route.auth

import com.example.domain.token.TokenId
import com.example.domain.token.TokenProvider
import com.example.domain.token.TokenValidator
import com.example.domain.user.UserAuthorizationService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

fun Routing.authorization(
    userAuthorizationService: UserAuthorizationService,
    tokenProvider: TokenProvider,
    tokenValidator: TokenValidator,
) {
    route("/oauth") {
        authenticate("auth-oauth-google") {
            get("/google") {
                val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()!!
                val user = userAuthorizationService.byGoogleOAuth(principal.accessToken)

                val (accessToken, refreshToken) = tokenProvider.issueAll(TokenId(user.id))
                call.respond(TokenResult(accessToken.value, refreshToken.value))
            }
        }
    }

    post("/refresh-tokens") {
        val tokens = call.receive<RefreshRequestedTokens>()
        tokenValidator
            .validate(tokens.accessToken, tokens.refreshToken)
            .onLeft {
                when (it) {
                    is TokenValidator.Error.Expired -> {
                        val (accessToken, refreshToken) = tokenProvider.issueAll(it.tokenId)
                        call.respond(TokenResult(accessToken.value, refreshToken.value))
                    }

                    else -> {
                        call.respond(HttpStatusCode.Unauthorized)
                    }
                }
            }.onRight {
                call.respond(TokenResult(tokens.accessToken, tokens.refreshToken))
            }
    }
}

@Serializable
private data class TokenResult(
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
private data class RefreshRequestedTokens(
    val accessToken: String,
    val refreshToken: String,
)

fun Routing.authentication() {
    authenticate("auth-jwt") {
    }
}
