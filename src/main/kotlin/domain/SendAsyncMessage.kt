package com.example.domain

fun interface SendAsyncMessage {
    suspend operator fun invoke(message: AsyncMessage)
}

interface AsyncMessage
