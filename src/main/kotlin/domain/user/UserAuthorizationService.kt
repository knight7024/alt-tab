package com.example.domain.user

class UserAuthorizationService(
    private val googleEmailRepository: UserEmailRepository,
    private val userRepository: UserRepository,
) {
    suspend fun byGoogleOAuth(accessToken: String): User {
        val user =
            googleEmailRepository
                .findByAccessToken(accessToken)
                .let {
                    userRepository.findByEmail(it)
                        ?: userRepository.signUp(it)
                }

        return user
    }
}
