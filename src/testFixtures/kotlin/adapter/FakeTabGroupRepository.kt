package adapter

import com.example.domain.extension.BrowserTabInfo
import com.example.domain.extension.TabGroup
import com.example.domain.extension.TabGroupRepository
import com.example.domain.user.UserId

class FakeTabGroupRepository : TabGroupRepository {
    private var id = 0L
    private val tabGroups = mutableMapOf<Long, TabGroup>()

    override suspend fun find(id: Long): TabGroup? = tabGroups[id]

    override suspend fun findAllByUserId(userId: UserId): List<TabGroup> = tabGroups.values.filter { it.userId == userId }

    override suspend fun save(
        userId: UserId,
        secret: String,
        salt: String,
        tabs: Collection<BrowserTabInfo>,
    ) {
        with(++id) {
            tabGroups[this] =
                TabGroup(
                    id = id,
                    userId = userId,
                    secret = secret,
                    salt = salt,
                    tabs = tabs.toList(),
                )
        }
    }

    override suspend fun remove(id: Long) {
        tabGroups.remove(id)
    }
}
