package com.example.config

data class AppConfig(
    val jwt: JwtConfig,
    val mongoUser: MongoConfig,
    val mongoRefreshToken: MongoConfig,
    val mongoBrowserTabInfo: MongoConfig,
    val mongoStashSetting: MongoConfig,
    val oAuthGoogle: OAuthConfig,
    val googleUrl: UrlConfig,
)
