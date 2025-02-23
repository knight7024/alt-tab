package com.example.module

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.config
import com.example.secretConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache5.Apache5
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.oauth
import io.ktor.server.config.tryGetString

internal fun Application.configureSecurity() {
    val jwtAudience = config.tryGetString("jwt.audience")!!
    val jwtIssuer = config.tryGetString("jwt.issuer")!!
    val jwtRealm = config.tryGetString("jwt.realm")!!
    val jwtSecret = secretConfig.tryGetString("jwt.secret")!!
    authentication {
        jwt {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build(),
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
    }

    authentication {
        oauth("auth-oauth-google") {
            urlProvider = { "http://localhost:8080/auth/google" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                    accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = secretConfig.tryGetString("oauth-google.client-id")!!,
                    clientSecret = secretConfig.tryGetString("oauth-google.client-secret")!!,
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
