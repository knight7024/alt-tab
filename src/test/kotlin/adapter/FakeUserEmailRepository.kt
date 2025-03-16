package com.example.adapter

import com.example.domain.user.UserEmailRepository

class FakeUserEmailRepository : UserEmailRepository {
    private val emailByAccessToken = mutableMapOf<String, String>()

    override suspend fun findByAccessToken(accessToken: String): String = emailByAccessToken[accessToken]!!

    fun save(
        accessToken: String,
        email: String,
    ) {
        emailByAccessToken[accessToken] = email
    }
}
