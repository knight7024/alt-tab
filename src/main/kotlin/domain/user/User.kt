package com.example.domain.user

import java.time.Instant

data class User(
    val uuid: String,
    val signedUpAt: Instant,
)
