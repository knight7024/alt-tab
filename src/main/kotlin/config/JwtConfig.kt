package com.example.config

data class JwtConfig(
    val issuer: String,
    val accessTokenSecret: String,
    val refreshTokenSecret: String,
)
