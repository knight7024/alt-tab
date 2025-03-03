package com.example.domain.user

import java.time.Instant

data class User(
    val id: String,
    val email: String,
    val signedUpAt: Instant,
)
