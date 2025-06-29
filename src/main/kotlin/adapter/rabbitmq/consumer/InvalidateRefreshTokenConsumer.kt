package com.example.adapter.rabbitmq.consumer

import com.example.adapter.rabbitmq.MessageHandler
import com.example.adapter.rabbitmq.QueueName
import com.example.adapter.rabbitmq.RabbitChannelPool
import com.example.adapter.rabbitmq.service.InvalidateRefreshTokenMessage
import com.example.config.RabbitMqConfig
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
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
    private val channelPool: RabbitChannelPool,
    private val consumerConfig: RabbitMqConfig.ConsumerConfig,
    private val handler: MessageHandler<InvalidateRefreshTokenMessage>,
) : AutoCloseable {
    private val queueName = QueueName.INVALIDATE_REFRESH_TOKENS
    private val borrowedChannels = ConcurrentHashMap<String, Channel>()
    private val consumerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        consumerScope.launch {
            repeat(consumerConfig.concurrentConsumers) { consumerIndex ->
                val consumerTag = "consumer-$queueName-$consumerIndex"
                val channel = channelPool.borrowChannel()
                borrowedChannels[consumerTag] = channel
                channel.basicQos(consumerConfig.prefetchCount)

                val consumer =
                    object : DefaultConsumer(channel) {
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
                channel.basicConsume(queueName, false, consumerTag, consumer)
            }
        }
    }

    override fun close() {
        // 코루틴 스코프 먼저 종료해야 데이터가 더이상 컨슘되지 않는다.
        consumerScope.cancel()
        borrowedChannels.entries.forEach { (consumerTag, channel) ->
            channel.basicCancel(consumerTag)
            channelPool.returnChannel(channel)
        }
    }
}
