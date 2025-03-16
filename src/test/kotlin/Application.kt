package com.example

import com.example.adapter.FakeUserEmailRepository
import com.example.config.AppConfig
import com.example.config.JwtConfig
import com.example.config.MongoConfig
import com.example.config.OAuthConfig
import com.example.config.UrlConfig
import com.example.domain.token.FakeRefreshTokenRepository
import com.example.domain.token.TokenProvider
import com.example.domain.token.TokenValidator
import com.example.domain.user.FakeUserRepository
import com.example.domain.user.UserAuthorizationService
import com.example.module.configureHTTP
import com.example.module.configureRouting
import com.example.module.configureSecurity
import com.example.module.configureSerialization
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.tryGetString
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

private fun Application.testModule() {
    // config
    val appConfig =
        AppConfig(
            jwt =
                JwtConfig(
                    issuer = config.tryGetString("jwt.issuer")!!,
                    accessTokenSecret = secretConfig.tryGetString("jwt.access-token-secret")!!,
                    refreshTokenSecret = secretConfig.tryGetString("jwt.refresh-token-secret")!!,
                ),
            mongoUser =
                MongoConfig(
                    uri = secretConfig.tryGetString("mongodb-users.uri")!!,
                    database = secretConfig.tryGetString("mongodb-users.database")!!,
                    collection = secretConfig.tryGetString("mongodb-users.collection")!!,
                ),
            mongoRefreshToken =
                MongoConfig(
                    uri = secretConfig.tryGetString("mongodb-refresh-tokens.uri")!!,
                    database = secretConfig.tryGetString("mongodb-refresh-tokens.database")!!,
                    collection = secretConfig.tryGetString("mongodb-refresh-tokens.collection")!!,
                ),
            oAuthGoogle =
                OAuthConfig(
                    clientId = secretConfig.tryGetString("oauth-google.client-id")!!,
                    clientSecret = secretConfig.tryGetString("oauth-google.client-secret")!!,
                ),
            googleUrl =
                UrlConfig(
                    baseUrl = config.tryGetString("google.baseUrl")!!,
                ),
        )

    // dependency
    val userEmailRepository = FakeUserEmailRepository()
    val userRepository = FakeUserRepository()
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

    val userAuthorizationService = UserAuthorizationService(userEmailRepository, userRepository, clock)

    val tokenProvider = TokenProvider(appConfig.jwt, clock)
    val tokenValidator = TokenValidator(appConfig.jwt, clock)
    val refreshTokenRepository = FakeRefreshTokenRepository()

    // configure
    configureSecurity(
        jwtConfig = appConfig.jwt,
        oAuthGoogleConfig = appConfig.oAuthGoogle,
    )
    configureHTTP()
    configureSerialization()
    configureRouting(
        userAuthorizationService = userAuthorizationService,
        tokenProvider = tokenProvider,
        tokenValidator = tokenValidator,
        refreshTokenRepository = refreshTokenRepository,
    )
}

private val config = ApplicationConfig("application.conf")
private val secretConfig = ApplicationConfig("secrets.conf")

internal fun baseTestApplication(block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) =
    testApplication {
        application {
            testModule()
        }
        val client =
            createClient {
                install(ContentNegotiation) {
                    json()
                }
                defaultRequest {
                    contentType(ContentType.Application.Json)
                }
            }
        block(client)
    }
