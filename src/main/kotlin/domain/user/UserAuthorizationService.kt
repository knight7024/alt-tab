package com.example.domain.user

import org.bson.types.ObjectId
import java.time.Clock

class UserAuthorizationService(
    private val googleEmailRepository: UserEmailRepository,
    private val userRepository: UserRepository,
    private val clock: Clock,
) {
    suspend fun byGoogleOAuth(accessToken: String): User {
        val user =
            googleEmailRepository
                .findByAccessToken(accessToken)
                .let {
                    userRepository.findByEmail(it)
                        ?: signUp(it)
                }

        return user
    }

    private suspend fun signUp(email: String): User =
        User(
            id = ObjectId().toHexString(),
            email = email,
            signedUpAt = clock.instant(),
        ).also { userRepository.save(it) }
}
