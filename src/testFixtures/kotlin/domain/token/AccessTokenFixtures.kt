package domain.token

import com.example.domain.token.AccessToken
import java.util.UUID

object AccessTokenFixtures {
    fun dummy(accessToken: String = UUID.randomUUID().toString()) =
        AccessToken(accessToken)
}
