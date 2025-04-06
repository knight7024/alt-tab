package com.example

import com.example.adapter.http.GoogleClient
import com.example.adapter.mongodb.GenerateTabGroupId
import com.example.adapter.mongodb.MongoRefreshTokenRepository
import com.example.adapter.mongodb.MongoStashSettingRepository
import com.example.adapter.mongodb.MongoTabGroupRepository
import com.example.adapter.mongodb.MongoUserRepository
import com.example.adapter.rabbitmq.consumer.InvalidateRefreshTokenConsumer
import com.example.adapter.rabbitmq.producer.InvalidateRefreshTokenProducer
import com.example.adapter.rabbitmq.service.InvalidateRefreshTokenService
import com.example.config.AppConfig
import com.example.config.JwtConfig
import com.example.config.MongoConfig
import com.example.config.OAuthConfig
import com.example.config.RabbitMqConfig
import com.example.config.UrlConfig
import com.example.domain.token.TokenProvider
import com.example.domain.token.TokenValidator
import com.example.domain.user.UserAuthorizationService
import com.example.module.configureHTTP
import com.example.module.configureRouting
import com.example.module.configureSecurity
import com.example.module.configureSerialization
import com.example.module.counterDao
import com.example.module.refreshTokenDao
import com.example.module.stashSettingDao
import com.example.module.tabGroupDao
import com.example.module.userDao
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
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
            mongoTabGroup =
                MongoConfig(
                    uri = secretConfig.tryGetString("mongodb-tab-groups.uri")!!,
                    database = secretConfig.tryGetString("mongodb-tab-groups.database")!!,
                    collection = secretConfig.tryGetString("mongodb-tab-groups.collection")!!,
                ),
            mongoStashSetting =
                MongoConfig(
                    uri = secretConfig.tryGetString("mongodb-stash-settings.uri")!!,
                    database = secretConfig.tryGetString("mongodb-stash-settings.database")!!,
                    collection = secretConfig.tryGetString("mongodb-stash-settings.collection")!!,
                ),
            mongoCounter =
                MongoConfig(
                    uri = secretConfig.tryGetString("mongodb-counters.uri")!!,
                    database = secretConfig.tryGetString("mongodb-counters.database")!!,
                    collection = secretConfig.tryGetString("mongodb-counters.collection")!!,
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
            rabbitMq =
                RabbitMqConfig(
                    server =
                        RabbitMqConfig.ServerConfig(
                            host = secretConfig.tryGetString("rabbitmq-server.host")!!,
                            port = secretConfig.tryGetString("rabbitmq-server.port")!!.toInt(),
                            virtualHost = secretConfig.tryGetString("rabbitmq-server.virtual-host")!!,
                            user = secretConfig.tryGetString("rabbitmq-server.user")!!,
                            password = secretConfig.tryGetString("rabbitmq-server.password")!!,
                        ),
                    consumers =
                        RabbitMqConfig.Consumers(
                            invalidateRefreshToken =
                                RabbitMqConfig.ConsumerConfig(
                                    queueName = secretConfig.tryGetString("rabbitmq-consumers.invalidate-refresh-tokens.queue")!!,
                                    concurrentConsumers =
                                        secretConfig
                                            .tryGetString("rabbitmq-consumers.invalidate-refresh-tokens.concurrent-consumers")!!
                                            .toInt(),
                                    prefetchCount =
                                        secretConfig
                                            .tryGetString("rabbitmq-consumers.invalidate-refresh-tokens.prefetch-count")!!
                                            .toInt(),
                                ),
                        ),
                ),
        )

    // dependency
    val clock = Clock.systemDefaultZone()
    val userEmailRepository = GoogleClient(appConfig.googleUrl.baseUrl)
    val userRepository = MongoUserRepository(userDao(appConfig.mongoUser), clock)
    val stashSettingRepository = MongoStashSettingRepository(stashSettingDao(appConfig.mongoStashSetting))

    val userAuthorizationService = UserAuthorizationService(userEmailRepository, userRepository)

    val tokenProvider = TokenProvider(appConfig.jwt, clock)
    val tokenValidator = TokenValidator(appConfig.jwt, clock)
    val refreshTokenRepository = MongoRefreshTokenRepository(refreshTokenDao(appConfig.mongoRefreshToken))

    val generateTabGroupId = GenerateTabGroupId(counterDao(appConfig.mongoCounter))
    val tabGroupRepository = MongoTabGroupRepository(tabGroupDao(appConfig.mongoTabGroup), generateTabGroupId)

    val invalidateRefreshTokenConsumer =
        InvalidateRefreshTokenConsumer(appConfig.rabbitMq, InvalidateRefreshTokenService(refreshTokenRepository))
    val invalidateRefreshTokenProducer = InvalidateRefreshTokenProducer(appConfig.rabbitMq)

    // configure
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
        invalidateRefreshToken = invalidateRefreshTokenProducer,
        tabGroupRepository = tabGroupRepository,
        clock = clock,
    )

    monitor.subscribe(ApplicationStopped) {
        invalidateRefreshTokenConsumer.close()
    }
}

private val config = ApplicationConfig("application.conf")
private val secretConfig = ApplicationConfig("secrets.conf")
