package com.example.domain.token

import com.example.domain.user.UserId
import java.time.Duration
import java.time.Instant
import java.util.UUID

data class TokenId(
    val userId: UserId,
    val pairingKey: String = UUID.randomUUID().toString().replace("-", ""),
)

@ConsistentCopyVisibility
data class RefreshToken internal constructor(
    val value: String,
    val tokenId: TokenId,
    val expiresIn: Instant,
) {
    companion object {
        internal val EXPIRES_IN = Duration.ofHours(6)
    }
}

@ConsistentCopyVisibility
data class AccessToken internal constructor(
    val value: String,
    val tokenId: TokenId,
) {
    companion object {
        internal val EXPIRES_IN = Duration.ofMinutes(15)
    }
}
