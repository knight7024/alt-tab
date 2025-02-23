package com.example.domain

import java.time.Instant

data class User(
    val uuid: String,
    val signedUpAt: Instant,
)
