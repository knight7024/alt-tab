package com.example.domain

import java.time.Clock

class UserAuthenticationService(
    private val googleEmailRepository: UserEmailRepository,
    private val userRepository: UserRepository,
    private val clock: Clock,
) {
    suspend fun byGoogleOAuth(accessToken: String): User {
        val user =
            googleEmailRepository
                .findByAccessToken(accessToken)
                .let {
                    userRepository.findByUuid(it)
                        ?: signUp(it)
                }

        return user
    }

    private suspend fun signUp(uuid: String): User =
        User(
            uuid = uuid,
            signedUpAt = clock.instant(),
        ).also { userRepository.save(it) }
}
