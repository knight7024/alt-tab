package com.example.adapter.rabbitmq.consumer

import com.example.adapter.rabbitmq.MessageHandler
import com.example.adapter.rabbitmq.service.InvalidateRefreshTokenMessage
import com.example.config.RabbitMqConfig
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class InvalidateRefreshTokenConsumer(
    private val config: RabbitMqConfig,
    private val handler: MessageHandler<InvalidateRefreshTokenMessage>,
) : AutoCloseable {
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
    private val channels = ConcurrentHashMap<String, Pair<Connection, Channel>>()
    private val consumerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        consumerScope.launch {
            repeat(config.consumers.invalidateRefreshToken.concurrentConsumers) { consumerIndex ->
                val connection = connectionFactory.newConnection()
                connection
                    .createChannel()
                    .apply {
                        basicQos(config.consumers.invalidateRefreshToken.prefetchCount)
                    }.also {
                        val consumerTag = "consumer-${config.consumers.invalidateRefreshToken.queueName}-$consumerIndex"
                        val consumer =
                            object : DefaultConsumer(it) {
                                override fun handleDelivery(
                                    consumerTag: String,
                                    envelope: Envelope,
                                    properties: AMQP.BasicProperties,
                                    body: ByteArray,
                                ) {
                                    launch(consumerScope.coroutineContext) {
                                        runCatching {
                                            val message = Json.decodeFromString<InvalidateRefreshTokenMessage>(body.decodeToString())
                                            handler.handle(message)
                                        }.onSuccess {
                                            channel.basicAck(envelope.deliveryTag, false)
                                        }.onFailure {
                                            channel.basicNack(envelope.deliveryTag, false, false)
                                        }
                                    }
                                }
                            }

                        it.basicConsume(config.consumers.invalidateRefreshToken.queueName, false, consumerTag, consumer)
                        channels[consumerTag] = connection to it
                    }
            }
        }
    }

    override fun close() {
        // 코루틴 스코프 먼저 종료해야 데이터가 더이상 컨슘되지 않는다.
        consumerScope.cancel()
        channels.forEach { (consumerTag, connectionAndChannel) ->
            val (connection, channel) = connectionAndChannel
            runCatching {
                if (channel.isOpen) {
                    channel.basicCancel(consumerTag)
                    channel.close()
                }
            }
            runCatching {
                if (connection.isOpen) {
                    connection.close()
                }
            }
        }
        channels.clear()
    }
}
