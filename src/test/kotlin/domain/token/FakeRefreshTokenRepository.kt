package com.example.domain.token

class FakeRefreshTokenRepository : RefreshTokenRepository {
    override suspend fun save(token: RefreshToken) {
        TODO("Not yet implemented")
    }

    override suspend fun invalidateOnce(token: RefreshToken): Boolean {
        TODO("Not yet implemented")
    }
}
