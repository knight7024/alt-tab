package com.example.module

import com.example.config.OAuthConfig
import com.example.domain.token.TokenValidator
import com.example.domain.user.User
import com.example.domain.user.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache5.Apache5
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.authentication
import io.ktor.server.auth.bearer
import io.ktor.server.auth.oauth
import io.ktor.server.auth.principal
import io.ktor.server.routing.RoutingCall

/**
 * @see com.example.domain.token.TokenProvider
 * @see com.example.domain.token.TokenValidator
 */
internal fun Application.configureSecurity(
    oAuthGoogleConfig: OAuthConfig,
    tokenValidator: TokenValidator,
    userRepository: UserRepository,
) {
    authentication {
        bearer("auth-bearer") {
            authenticate { tokenCredential ->
                val accessToken = tokenCredential.token
                tokenValidator
                    .validateAccessToken(accessToken)
                    .onLeft { return@authenticate null }
                    .onRight {
                        val user = userRepository.find(it.tokenId.userId)
                        return@authenticate user?.let { UserPrincipal(it) }
                    }
            }
        }
    }

    authentication {
        oauth("auth-oauth-google") {
            urlProvider = { "http://localhost:8080/oauth/google" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                    accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = oAuthGoogleConfig.clientId,
                    clientSecret = oAuthGoogleConfig.clientSecret,
                    defaultScopes =
                        listOf(
                            "https://www.googleapis.com/auth/userinfo.profile",
                            "https://www.googleapis.com/auth/userinfo.email",
                        ),
                )
            }
            client = HttpClient(Apache5)
        }
    }
}

internal fun RoutingCall.authenticatedUser() = requireNotNull(principal<UserPrincipal>()).user

data class UserPrincipal(
    val user: User,
)
