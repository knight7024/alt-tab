package com.example.domain

import java.time.Clock

class CreateOrLogin(
    private val userEmailRepository: UserEmailRepository,
    private val userRepository: UserRepository,
    private val clock: Clock
) {
    suspend fun byGoogle(accessToken: String): User {
        val user = userEmailRepository.findByAccessToken(accessToken)!!
            .let {
                userRepository.findByUuid(it)
                    ?: signUp(it)
            }

        return user
    }

    private suspend fun signUp(uuid: String): User =
        User(
            uuid = uuid,
            signedUpAt = clock.instant()
        ).also { userRepository.save(it) }
}