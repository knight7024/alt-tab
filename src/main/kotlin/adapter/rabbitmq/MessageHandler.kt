package com.example.adapter.rabbitmq

import com.example.domain.AsyncMessage

fun interface MessageHandler<T : AsyncMessage> {
    suspend fun handle(message: T)
}
