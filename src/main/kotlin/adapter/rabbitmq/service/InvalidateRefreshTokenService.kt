package com.example.adapter.rabbitmq.service

import com.example.adapter.rabbitmq.MessageHandler
import com.example.domain.AsyncMessage
import com.example.domain.token.RefreshTokenRepository
import com.example.domain.token.TokenId
import com.example.domain.user.UserId
import kotlinx.serialization.Serializable

class InvalidateRefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
) : MessageHandler<InvalidateRefreshTokenMessage> {
    override suspend fun handle(message: InvalidateRefreshTokenMessage) {
        refreshTokenRepository.invalidateAll(
            TokenId(
                userId = UserId(message.userId),
                pairingKey = message.pairingKey,
            ),
        )
    }
}

@Serializable
data class InvalidateRefreshTokenMessage(
    val userId: String,
    val pairingKey: String,
) : AsyncMessage
