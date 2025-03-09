package com.example.adapter

import com.example.domain.user.UserEmailRepository

class FakeGoogleClient : UserEmailRepository {
    override suspend fun findByAccessToken(accessToken: String): String {
        TODO("Not yet implemented")
    }
}
