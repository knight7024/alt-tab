package com.example.domain.user

import java.time.Instant

data class User(
    val id: UserId,
    val email: String,
    val signedUpAt: Instant,
)

@JvmInline
value class UserId internal constructor(
    val value: String,
)
