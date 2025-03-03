package com.example.config

data class JwtConfig(
    val audience: String,
    val issuer: String,
    val secret: String,
)
