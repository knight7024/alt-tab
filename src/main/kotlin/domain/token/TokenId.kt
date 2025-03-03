package com.example.domain.token

import com.example.domain.user.UserId
import java.util.UUID

data class TokenId(
    val userId: UserId,
    val pairingKey: String = UUID.randomUUID().toString().replace("-", ""),
)
