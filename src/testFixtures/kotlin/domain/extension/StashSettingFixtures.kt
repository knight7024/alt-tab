package domain.extension

import com.example.domain.extension.StashRule
import com.example.domain.extension.StashSetting
import com.example.domain.user.UserId
import java.time.Duration
import java.util.UUID
import kotlin.random.Random

object StashSettingFixtures {
    fun dummyStashSetting(userId: UserId) =
        StashSetting(
            userId = userId,
            globalRule = dummyStashRule(),
            whitelistUrls =
                mutableMapOf<String, StashRule?>().also { list ->
                    repeat(Random.nextInt(0, 3)) {
                        list[UUID.randomUUID().toString()] = dummyStashRule()
                    }
                },
        )

    fun dummyStashRule() =
        StashRule(
            idleCondition = listOf("window", "visiblity", "idle").random(),
            idleTimeout = Random.nextLong(10, 61).let { Duration.ofMinutes(it) },
            ignoreUnloadedTab = Random.nextBoolean(),
            ignoreAudibleTab = Random.nextBoolean(),
            ignoreContainerTab = Random.nextBoolean(),
            allowPinnedTab = Random.nextBoolean(),
        )
}
