package com.example

import BootstrapService
import FakeClock
import adapter.FakeRefreshTokenRepository
import adapter.FakeStashSettingRepository
import adapter.FakeUserEmailRepository
import adapter.FakeUserRepository
import com.example.config.AppConfig
import com.example.config.JwtConfig
import com.example.config.MongoConfig
import com.example.config.OAuthConfig
import com.example.config.UrlConfig
import com.example.domain.token.TokenProvider
import com.example.domain.token.TokenValidator
import com.example.domain.user.UserAuthorizationService
import com.example.module.configureHTTP
import com.example.module.configureRouting
import com.example.module.configureSecurity
import com.example.module.configureSerialization
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.bearerAuth
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.tryGetString
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication

private fun Application.testModule() {
    configureSecurity(
        oAuthGoogleConfig = appConfig.oAuthGoogle,
        tokenValidator = tokenValidator,
        userRepository = userRepository,
    )
    configureHTTP()
    configureSerialization()
    configureRouting(
        userAuthorizationService = userAuthorizationService,
        stashSettingRepository = stashSettingRepository,
        tokenProvider = tokenProvider,
        tokenValidator = tokenValidator,
        refreshTokenRepository = refreshTokenRepository,
    )
}

private val config = ApplicationConfig("application.conf")
private val secretConfig = ApplicationConfig("secrets.conf")

// config
private val appConfig =
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
        mongoBrowserTabInfo =
            MongoConfig(
                uri = secretConfig.tryGetString("mongodb-browser-tab-info.uri")!!,
                database = secretConfig.tryGetString("mongodb-browser-tab-info.database")!!,
                collection = secretConfig.tryGetString("mongodb-browser-tab-info.collection")!!,
            ),
        mongoStashSetting =
            MongoConfig(
                uri = secretConfig.tryGetString("mongodb-stash-setting.uri")!!,
                database = secretConfig.tryGetString("mongodb-stash-setting.database")!!,
                collection = secretConfig.tryGetString("mongodb-stash-setting.collection")!!,
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
internal val clock = FakeClock()
internal val userEmailRepository = FakeUserEmailRepository()
internal val userRepository = FakeUserRepository(clock)
internal val stashSettingRepository = FakeStashSettingRepository()

internal val userAuthorizationService = UserAuthorizationService(userEmailRepository, userRepository)

internal val tokenProvider = TokenProvider(appConfig.jwt, clock)
internal val tokenValidator = TokenValidator(appConfig.jwt, clock)
internal val refreshTokenRepository = FakeRefreshTokenRepository()

internal val hashIdCodec = HashIdCodec(clock)
internal val bootstrapService = BootstrapService(tokenProvider, userRepository, refreshTokenRepository, stashSettingRepository)

internal fun baseTestApplication(
    accessToken: String? = null,
    block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit,
) = testApplication {
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
                if (accessToken != null) {
                    bearerAuth(accessToken)
                }
            }
        }
    block(client)
}
