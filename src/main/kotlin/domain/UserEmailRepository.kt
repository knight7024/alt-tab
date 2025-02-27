package com.example.domain

interface UserEmailRepository {
    suspend fun findByAccessToken(accessToken: String): String
}
