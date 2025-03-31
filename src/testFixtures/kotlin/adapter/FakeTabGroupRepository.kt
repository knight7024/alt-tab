package adapter

import com.example.domain.extension.BrowserTabInfo
import com.example.domain.extension.HashIdCodec
import com.example.domain.extension.TabGroup
import com.example.domain.extension.TabGroupRepository
import com.example.domain.user.UserId

class FakeTabGroupRepository(
    private val hashIdCodec: HashIdCodec,
) : TabGroupRepository {
    private var id = 0L
    private val tabGroups = mutableMapOf<Long, TabGroup>()

    override suspend fun find(id: String): TabGroup? = hashIdCodec.decode(id).getOrNull()?.let { tabGroups[it] }

    override suspend fun findAllByUserId(userId: UserId): List<TabGroup> = tabGroups.values.filter { it.userId == userId }

    override suspend fun save(
        userId: UserId,
        secret: String,
        salt: String,
        tabs: Collection<BrowserTabInfo>,
    ): String {
        with(++id) {
            val encodedId = hashIdCodec.encode(this)
            tabGroups[this] =
                TabGroup(
                    id = encodedId,
                    userId = userId,
                    secret = secret,
                    salt = salt,
                    tabs = tabs.toList(),
                )
            return encodedId
        }
    }

    override suspend fun remove(id: String) {
        hashIdCodec.decode(id).getOrNull()?.let { tabGroups.remove(it) }
    }
}
