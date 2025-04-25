package com.example.adapter.rabbitmq

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import org.apache.commons.pool2.impl.GenericObjectPool
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

class RabbitChannelPool(
    connectionFactory: ConnectionFactory,
    poolConfig: ChannelPoolConfig,
) : AutoCloseable {
    private val channelFactory = ReusableChannelFactory { connectionFactory.newConnection().createChannel() }
    private val pool =
        GenericObjectPool(
            channelFactory,
            GenericObjectPoolConfig<Channel>().apply {
                minIdle = poolConfig.minIdle
                maxIdle = poolConfig.maxIdle
                maxTotal = poolConfig.maxTotal
                setMaxWait(poolConfig.maxWait.toJavaDuration())
                testOnBorrow = true
                testOnReturn = true
            },
        )

    inline fun <R> useChannel(block: (Channel) -> R): R {
        val channel = borrowChannel()
        return try {
            block(channel)
        } finally {
            returnChannel(channel)
        }
    }

    fun borrowChannel(): Channel = pool.borrowObject()

    fun returnChannel(channel: Channel) {
        pool.returnObject(channel)
    }

    override fun close() {
        pool.close()
    }
}

data class ChannelPoolConfig(
    val minIdle: Int = 0,
    val maxIdle: Int = 8,
    val maxTotal: Int = 8,
    val maxWait: Duration = (-1).milliseconds,
)
