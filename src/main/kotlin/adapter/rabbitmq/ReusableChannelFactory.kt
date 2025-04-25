package com.example.adapter.rabbitmq

import com.rabbitmq.client.Channel
import org.apache.commons.pool2.BasePooledObjectFactory
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.impl.DefaultPooledObject
import org.slf4j.LoggerFactory
import java.util.function.Supplier

class ReusableChannelFactory(
    private val channelSupplier: Supplier<Channel>,
) : BasePooledObjectFactory<Channel>() {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun create(): Channel = channelSupplier.get().also { logger.info("Creating channel `${it.channelNumber}`") }

    override fun wrap(channel: Channel): PooledObject<Channel> = DefaultPooledObject(channel)

    override fun validateObject(p: PooledObject<Channel>): Boolean = p.`object`.isOpen

    override fun destroyObject(p: PooledObject<Channel>) {
        runCatching {
            val channel = p.`object`
            logger.info("Closing channel `${channel.channelNumber}`")
            channel.close()
        }
    }
}
