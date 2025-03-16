package domain.token

import com.example.domain.token.TokenId
import com.example.domain.user.UserId
import java.util.UUID

object TokenIdFixtures {
    fun dummy() =
        TokenId(
            userId = UserId(UUID.randomUUID().toString()),
            pairingKey = UUID.randomUUID().toString(),
        )
}
