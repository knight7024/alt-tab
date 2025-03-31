package com.example.adapter

import com.example.domain.extension.BrowserTabInfo
import com.example.domain.extension.RelativeRatio
import com.example.domain.extension.TabGroup
import com.example.domain.extension.TabGroupRepository
import com.example.domain.user.UserId
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

class MongoTabGroupRepository(
    private val dao: MongoCollection<TabGroupDocument>,
    private val generateTabGroupId: GenerateTabGroupId,
) : TabGroupRepository {
    override suspend fun find(id: Long): TabGroup? =
        dao
            .find(Filters.eq(TabGroupDocument.FIELD_ID, id))
            .first()
            ?.toDomain()

    override suspend fun findAllByUserId(userId: UserId): List<TabGroup> =
        dao
            .find(Filters.eq(TabGroupDocument.FIELD_USER_ID, userId.value))
            .map { it.toDomain() }
            .toList()

    override suspend fun save(
        userId: UserId,
        secret: String,
        salt: String,
        /* TODO: 양이 많을 수도, 사이즈가 클 수도 있다.
            chunk해서 저장하거나, 사이즈 제한을 걸어야 할 수도 있다. */
        tabs: Collection<BrowserTabInfo>,
    ) {
        dao.insertOne(
            TabGroupDocument(
                id = generateTabGroupId(),
                userId = userId.value,
                secret = secret,
                salt = salt,
                tabs = tabs.map { it.toDocument() },
            ),
        )
    }

    override suspend fun remove(id: Long) {
        dao.deleteOne(Filters.eq(TabGroupDocument.FIELD_ID, id))
    }

    private fun TabGroupDocument.toDomain() =
        TabGroup(
            id = id,
            userId = UserId(userId),
            secret = secret,
            salt = salt,
            tabs = tabs.map { it.toDomain() },
        )

    private fun BrowserTabInfoDocument.toDomain() =
        BrowserTabInfo(
            windowId = windowId,
            groupId = groupId,
            tabIndex = tabIndex,
            title = title,
            url = url,
            faviconUrl = faviconUrl,
            incognito = incognito,
            scrollPosition =
                RelativeRatio(
                    x = scrollPosition.x,
                    y = scrollPosition.y,
                ),
            lastUsedAgent = lastUsedAgent,
            lastActiveAt = Instant.ofEpochSecond(lastActiveAt),
            session = session,
            cookie = cookie,
        )

    private fun BrowserTabInfo.toDocument() =
        BrowserTabInfoDocument(
            windowId = windowId,
            groupId = groupId,
            tabIndex = tabIndex,
            title = title,
            url = url,
            faviconUrl = faviconUrl,
            incognito = incognito,
            scrollPosition =
                BrowserTabInfoDocument.RelativeRatio(
                    x = scrollPosition.x,
                    y = scrollPosition.y,
                ),
            lastUsedAgent = lastUsedAgent,
            lastActiveAt = lastActiveAt.epochSecond,
            session = session,
            cookie = cookie,
        )
}

@Serializable
data class TabGroupDocument(
    @SerialName(FIELD_ID)
    val id: Long,
    @SerialName(FIELD_USER_ID)
    val userId: String,
    @SerialName(FIELD_SECRET)
    val secret: String,
    @SerialName(FIELD_SALT)
    val salt: String,
    @SerialName(FIELD_BROWSER_TABS)
    val tabs: List<BrowserTabInfoDocument>,
) {
    companion object {
        const val FIELD_ID = "_id"
        const val FIELD_USER_ID = "user_id"
        const val FIELD_SECRET = "secret"
        const val FIELD_SALT = "salt"
        const val FIELD_BROWSER_TABS = "browser_tabs"
    }
}

@Serializable
data class BrowserTabInfoDocument(
    @SerialName(FIELD_WINDOW_ID)
    val windowId: String,
    @SerialName(FIELD_GROUP_ID)
    val groupId: String?,
    @SerialName(FIELD_TAB_INDEX)
    val tabIndex: Int,
    @SerialName(FIELD_TITLE)
    val title: String,
    @SerialName(FIELD_URL)
    val url: String,
    @SerialName(FIELD_FAVICON_URL)
    val faviconUrl: String?,
    @SerialName(FIELD_INCOGNITO)
    val incognito: Boolean,
    @SerialName(FIELD_SCROLL_POSITION)
    val scrollPosition: RelativeRatio,
    @SerialName(FIELD_LAST_USED_AGENT)
    val lastUsedAgent: String,
    @SerialName(FIELD_LAST_ACTIVE_AT)
    val lastActiveAt: Long,
    @SerialName(FIELD_SESSION)
    val session: String,
    @SerialName(FIELD_COOKIE)
    val cookie: String,
) {
    @Serializable
    data class RelativeRatio(
        @SerialName(FIELD_X)
        val x: Double,
        @SerialName(FIELD_Y)
        val y: Double,
    ) {
        companion object {
            const val FIELD_X = "x"
            const val FIELD_Y = "y"
        }
    }

    companion object {
        const val FIELD_WINDOW_ID = "window_id"
        const val FIELD_GROUP_ID = "group_id"
        const val FIELD_TAB_INDEX = "tab_index"
        const val FIELD_TITLE = "title"
        const val FIELD_URL = "url"
        const val FIELD_FAVICON_URL = "favicon_url"
        const val FIELD_INCOGNITO = "incognito"
        const val FIELD_SCROLL_POSITION = "scroll_position"
        const val FIELD_LAST_USED_AGENT = "last_used_agent"
        const val FIELD_LAST_ACTIVE_AT = "last_active_at"
        const val FIELD_SESSION = "session"
        const val FIELD_COOKIE = "cookie"
    }
}
