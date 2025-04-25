package com.example.adapter.rabbitmq.producer

import com.example.adapter.rabbitmq.RabbitChannelPool
import com.example.adapter.rabbitmq.service.InvalidateRefreshTokenMessage
import com.example.config.RabbitMqConfig
import com.example.domain.AsyncMessage
import com.example.domain.SendAsyncMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class InvalidateRefreshTokenProducer(
    private val channelPool: RabbitChannelPool,
    private val consumerConfig: RabbitMqConfig.ConsumerConfig,
) : SendAsyncMessage,
    AutoCloseable {
    private val producerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override suspend fun invoke(message: AsyncMessage) {
        producerScope.launch {
            channelPool.useChannel { channel ->
                channel.basicPublish(
                    consumerConfig.queueName,
                    consumerConfig.queueName,
                    null,
                    Json.encodeToString(message as InvalidateRefreshTokenMessage).toByteArray(),
                )
            }
        }
    }

    override fun close() {
        producerScope.cancel()
    }
}
