import com.example.domain.extension.StashSettingRepository
import com.example.domain.token.AccessToken
import com.example.domain.token.RefreshToken
import com.example.domain.token.RefreshTokenRepository
import com.example.domain.token.TokenProvider
import com.example.domain.user.User
import com.example.domain.user.UserRepository
import domain.token.TokenIdFixtures
import java.util.UUID

class BootstrapService(
    private val tokenProvider: TokenProvider,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val stashSettingRepository: StashSettingRepository,
) {
    suspend fun signUp(): SignUpResult {
        val user = userRepository.signUp(UUID.randomUUID().toString())
        val tokenId = TokenIdFixtures.dummy(user.id)
        val (accessToken, refreshToken) = tokenProvider.issueAll(tokenId)

        refreshTokenRepository.save(refreshToken)
        stashSettingRepository.init(user.id)

        return SignUpResult(
            user = user,
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}

data class SignUpResult(
    val user: User,
    val accessToken: AccessToken,
    val refreshToken: RefreshToken,
)
