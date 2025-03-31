package domain.extension

import com.example.domain.extension.BrowserTabInfo
import com.example.domain.extension.RelativeRatio
import com.example.domain.extension.TabGroup
import com.example.domain.user.UserId
import java.time.Instant
import java.util.UUID
import kotlin.random.Random

object TabGroupFixtures {
    fun dummy(userId: UserId) =
        TabGroup(
            userId = userId,
            secret = UUID.randomUUID().toString(),
            salt = UUID.randomUUID().toString(),
            tabs =
                mutableListOf<BrowserTabInfo>().also { list ->
                    repeat(Random.nextInt(0, 3)) {
                        list += dummyBrowserTabInfo()
                    }
                },
        )

    fun dummyBrowserTabInfo() =
        BrowserTabInfo(
            windowId = UUID.randomUUID().toString(),
            groupId = UUID.randomUUID().toString(),
            tabIndex = Random.nextInt(0, Int.MAX_VALUE),
            title = UUID.randomUUID().toString(),
            url = UUID.randomUUID().toString(),
            faviconUrl = UUID.randomUUID().toString(),
            incognito = Random.nextBoolean(),
            scrollPosition =
                RelativeRatio(
                    x = Random.nextDouble(0.0, 1.0),
                    y = Random.nextDouble(0.0, 1.0),
                ),
            lastUsedAgent = UUID.randomUUID().toString(),
            lastActiveAt = Instant.now(),
            session = UUID.randomUUID().toString(),
            cookie = UUID.randomUUID().toString(),
        )
}
