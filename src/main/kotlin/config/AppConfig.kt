package com.example.config

data class AppConfig(
    val jwt: JwtConfig,
    val mongoUser: MongoConfig,
    val mongoRefreshToken: MongoConfig,
    val mongoTabGroup: MongoConfig,
    val mongoStashSetting: MongoConfig,
    val mongoCounter: MongoConfig,
    val oAuthGoogle: OAuthConfig,
    val googleUrl: UrlConfig,
    val rabbitMq: RabbitMqConfig,
)
