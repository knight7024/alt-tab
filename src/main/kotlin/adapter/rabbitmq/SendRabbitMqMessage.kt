package com.example.adapter.rabbitmq

import com.example.adapter.rabbitmq.service.InvalidateRefreshTokenMessage
import com.example.domain.AsyncMessage
import com.example.domain.SendAsyncMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SendRabbitMqMessage(
    private val channelPool: RabbitChannelPool,
) : SendAsyncMessage {
    private val producerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override suspend fun invoke(message: AsyncMessage) =
        when (message) {
            is InvalidateRefreshTokenMessage -> {
                publish<InvalidateRefreshTokenMessage>(
                    queueName = QueueName.INVALIDATE_REFRESH_TOKENS,
                    message = message,
                )
            }

            else -> throw IllegalArgumentException("Unknown message: ${message::class.simpleName}")
        }

    private inline fun <reified T : AsyncMessage> publish(queueName: String, message: T) {
        producerScope.launch {
            channelPool.useChannel { channel ->
                channel.basicPublish(
                    queueName,
                    queueName,
                    null,
                    Json.encodeToString(message).toByteArray(),
                )
            }
        }
    }
}
