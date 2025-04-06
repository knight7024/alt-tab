package com.example.adapter.rabbitmq.producer

import com.example.adapter.rabbitmq.service.InvalidateRefreshTokenMessage
import com.example.config.RabbitMqConfig
import com.example.domain.AsyncMessage
import com.example.domain.SendAsyncMessage
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicReference

class InvalidateRefreshTokenProducer(
    private val config: RabbitMqConfig,
) : SendAsyncMessage,
    AutoCloseable {
    private val connectionFactory: ConnectionFactory =
        ConnectionFactory().apply {
            host = config.server.host
            port = config.server.port
            virtualHost = config.server.virtualHost
            username = config.server.user
            password = config.server.password
            requestedHeartbeat = 60 // second
            connectionTimeout = 200 // second
            shutdownTimeout = 400 // second
        }

    @Volatile
    private lateinit var channel: AtomicReference<Channel>
    private val producerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        producerScope.launch {
            val producerTag = "producer-${config.consumers.invalidateRefreshToken.queueName}"
            val connection = connectionFactory.newConnection(producerTag)
            channel = AtomicReference(connection.createChannel())
        }
    }

    override suspend fun invoke(message: AsyncMessage) {
        producerScope.launch {
            val channel = channel.get()
            channel.basicPublish(
                config.consumers.invalidateRefreshToken.queueName,
                config.consumers.invalidateRefreshToken.queueName,
                null,
                Json.encodeToString(message as InvalidateRefreshTokenMessage).toByteArray(),
            )
        }
    }

    override fun close() {
        producerScope.cancel()
        val channel = channel.get()
        if (channel.isOpen) {
            channel.close()
        }
    }
}
