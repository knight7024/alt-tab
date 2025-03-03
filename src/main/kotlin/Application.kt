package com.example

import com.example.adapter.GoogleClient
import com.example.adapter.MongoUserRepository
import com.example.config.AppConfig
import com.example.config.JwtConfig
import com.example.config.MongoConfig
import com.example.config.OAuthConfig
import com.example.domain.TokenProvider
import com.example.domain.UserEmailRepository
import com.example.domain.UserRepository
import com.example.module.configureHTTP
import com.example.module.configureRouting
import com.example.module.configureSecurity
import com.example.module.configureSerialization
import com.example.module.userDao
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.tryGetString
import io.ktor.server.netty.EngineMain
import java.time.Clock

fun main(args: Array<String>) {
    EngineMain
        .main(args)
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
                    uri = secretConfig.tryGetString("mongodb-user.uri")!!,
                    database = secretConfig.tryGetString("mongodb-user.database")!!,
                    collection = secretConfig.tryGetString("mongodb-user.collection")!!,
                ),
            oAuthGoogle =
                OAuthConfig(
                    clientId = secretConfig.tryGetString("oauth-google.client-id")!!,
                    clientSecret = secretConfig.tryGetString("oauth-google.client-secret")!!,
                ),
        )

    // dependency
    val userEmailRepository: UserEmailRepository = GoogleClient(config.tryGetString("google.baseUrl")!!)
    val userRepository: UserRepository = MongoUserRepository(userDao(appConfig.mongoUser))
    val clock = Clock.systemDefaultZone()

    val tokenProvider = TokenProvider(appConfig.jwt, clock)

    // configure
    configureSecurity(
        jwtConfig = appConfig.jwt,
        oAuthGoogleConfig = appConfig.oAuthGoogle,
    )
    configureHTTP()
    configureSerialization()
    configureRouting(
        userEmailRepository = userEmailRepository,
        userRepository = userRepository,
        tokenProvider = tokenProvider,
        clock = clock,
    )
}

private val config = ApplicationConfig("application.conf")
private val secretConfig = ApplicationConfig("secrets.conf")
