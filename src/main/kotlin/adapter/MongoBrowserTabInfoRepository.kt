package com.example.adapter

import com.example.domain.extension.BrowserTabInfo
import com.example.domain.extension.BrowserTabInfoRepository
import com.example.domain.extension.RelativeRatio
import com.mongodb.client.MongoCollection
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.kotlinx.ObjectIdSerializer
import org.bson.types.ObjectId

class MongoBrowserTabInfoRepository(
    private val dao: MongoCollection<BrowserTabInfoDocument>,
) : BrowserTabInfoRepository {
    override suspend fun save(browserTabInfo: BrowserTabInfo) {
        dao.insertOne(browserTabInfo.toDocument())
    }

    private fun BrowserTabInfo.toDocument() =
        BrowserTabInfoDocument(
            id = ObjectId(id),
            userId = userId.value,
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
        )
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class BrowserTabInfoDocument(
    @SerialName(FIELD_ID)
    @Serializable(with = ObjectIdSerializer::class)
    val id: ObjectId,
    @SerialName(FIELD_USER_ID)
    val userId: String,
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
        const val FIELD_ID = "_id"
        const val FIELD_USER_ID = "user_id"
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
    }
}
