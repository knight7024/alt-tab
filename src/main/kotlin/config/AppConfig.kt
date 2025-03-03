package com.example.config

data class AppConfig(
    val jwt: JwtConfig,
    val mongoUser: MongoConfig,
)
