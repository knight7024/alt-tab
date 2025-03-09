package domain.token

import com.example.domain.token.RefreshToken
import com.example.domain.token.TokenId
import com.example.domain.user.UserId
import java.time.Instant
import java.util.UUID

object RefreshTokenFixtures {
    fun dummy(refreshToken: String = UUID.randomUUID().toString()) =
        RefreshToken(
            value = refreshToken,
            tokenId = TokenId(UserId(UUID.randomUUID().toString())),
            expiresIn = Instant.now() + RefreshToken.EXPIRES_IN
        )
}
