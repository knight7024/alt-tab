package com.example.domain.token

import java.util.UUID

data class TokenId(
    val userUuid: String,
    val pairingKey: String = UUID.randomUUID().toString().replace("-", ""),
)
