package domain.token

import com.example.domain.token.TokenId
import com.example.domain.user.UserId
import java.util.UUID

object TokenIdFixtures {
    fun dummy(userId: UserId = UserId(UUID.randomUUID().toString())) =
        TokenId(
            userId = userId,
            pairingKey = UUID.randomUUID().toString(),
        )
}
