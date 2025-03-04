package com.example

import com.example.adapter.GoogleClient
import com.example.adapter.MongoRefreshTokenRepository
import com.example.adapter.MongoUserRepository
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
import com.example.module.refreshTokenDao
import com.example.module.userDao
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.tryGetString
import io.ktor.server.netty.EngineMain
import java.time.Clock

fun main(args: Array<String>) {
    EngineMain.main(args)
}

internal fun Application.module() {
    // config
    val appConfig =
        AppConfig(
            jwt =
                JwtConfig(
                    audience = config.tryGetString("jwt.audience")!!,
                    issuer = config.tryGetString("jwt.issuer")!!,
                    secret = secretConfig.tryGetString("jwt.secret")!!,
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
    val userEmailRepository = GoogleClient(appConfig.googleUrl.baseUrl)
    val userRepository = MongoUserRepository(userDao(appConfig.mongoUser))
    val clock = Clock.systemDefaultZone()

    val userAuthorizationService = UserAuthorizationService(userEmailRepository, userRepository, clock)

    val tokenProvider = TokenProvider(appConfig.jwt, clock)
    val tokenValidator = TokenValidator(appConfig.jwt, clock)
    val refreshTokenRepository = MongoRefreshTokenRepository(refreshTokenDao(appConfig.mongoRefreshToken))

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
