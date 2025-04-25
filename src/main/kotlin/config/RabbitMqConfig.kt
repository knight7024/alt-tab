package com.example.config

data class RabbitMqConfig(
    val server: ServerConfig,
    val consumers: Consumers,
) {
    data class ServerConfig(
        val host: String,
        val port: Int,
        val virtualHost: String,
        val user: String,
        val password: String,
    )

    data class Consumers(
        val invalidateRefreshToken: ConsumerConfig,
    )

    data class ConsumerConfig(
        val queueName: String,
        val concurrentConsumers: Int,
        val prefetchCount: Int,
    )
}
