package com.example.adapter

import com.example.domain.extension.StashRule
import com.example.domain.extension.StashSetting
import com.example.domain.extension.StashSettingRepository
import com.example.domain.user.UserId
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.conversions.Bson
import java.time.Duration

class MongoStashSettingRepository(
    private val dao: MongoCollection<StashSettingDocument>,
) : StashSettingRepository {
    override suspend fun init(userId: UserId) {
        val defaultRule = StashRuleDocument.default()
        dao.insertOne(
            StashSettingDocument(
                userId = userId.value,
                globalRule = defaultRule,
                whitelistUrls = null,
            ),
        )
    }

    override suspend fun update(stashSetting: StashSetting) {
        val currentSetting = find(stashSetting.userId)
        val toBe = stashSetting.toDocument()
        val updateCandidates = mutableListOf<Bson>()

        if (currentSetting.globalRule != stashSetting.globalRule) {
            updateCandidates.add(
                Updates.set(
                    StashSettingDocument.FIELD_GLOBAL_RULE,
                    toBe.globalRule,
                ),
            )
        }
        if (currentSetting.whitelistUrls != stashSetting.whitelistUrls) {
            updateCandidates.add(
                Updates.set(
                    StashSettingDocument.FIELD_WHITELIST_URLS,
                    toBe.whitelistUrls,
                ),
            )
        }

        dao
            .updateOne(
                Filters.eq(StashSettingDocument.FIELD_USER_ID, stashSetting.userId.value),
                Updates.combine(*updateCandidates.toTypedArray()),
            )
    }

    override suspend fun find(userId: UserId): StashSetting =
        dao
            .find(
                Filters.eq(StashSettingDocument.FIELD_USER_ID, userId.value),
            ).first()!!
            .toDomain()

    private fun StashSetting.toDocument() =
        StashSettingDocument(
            userId = userId.value,
            globalRule = globalRule.toDocument(),
            whitelistUrls = whitelistUrls.mapValues { it.value?.toDocument() },
        )

    private fun StashRule.toDocument() =
        StashRuleDocument(
            idleCondition = idleCondition,
            idleTimeoutInMinutes = idleTimeout.toMinutes().toInt(),
            mutedTabIgnored = mutedTabIgnored,
            containerTabIgnored = containerTabIgnored,
            pinnedTabAllowed = pinnedTabAllowed,
        )

    private fun StashSettingDocument.toDomain() =
        StashSetting(
            userId = UserId(userId),
            globalRule = globalRule.toDomain(),
            whitelistUrls = whitelistUrls?.mapValues { it.value?.toDomain() } ?: emptyMap(),
        )

    private fun StashRuleDocument.toDomain() =
        StashRule(
            idleCondition = idleCondition,
            idleTimeout = Duration.ofMinutes(idleTimeoutInMinutes.toLong()),
            mutedTabIgnored = mutedTabIgnored,
            containerTabIgnored = containerTabIgnored,
            pinnedTabAllowed = pinnedTabAllowed,
        )
}

@Serializable
data class StashSettingDocument(
    @SerialName(FIELD_USER_ID)
    val userId: String,
    @SerialName(FIELD_GLOBAL_RULE)
    val globalRule: StashRuleDocument,
    @SerialName(FIELD_WHITELIST_URLS)
    val whitelistUrls: Map<String, StashRuleDocument?>?,
) {
    companion object {
        const val FIELD_USER_ID = "user_id"
        const val FIELD_GLOBAL_RULE = "global_rule"
        const val FIELD_WHITELIST_URLS = "whitelist_urls"
    }
}

@Serializable
data class StashRuleDocument(
    @SerialName(FIELD_IDLE_CONDITION)
    val idleCondition: String,
    @SerialName(FIELD_IDLE_TIMEOUT_IN_MINUTES)
    val idleTimeoutInMinutes: Int,
    @SerialName(FIELD_MUTED_TAB_IGNORED)
    val mutedTabIgnored: Boolean,
    @SerialName(FIELD_CONTAINER_TAB_IGNORED)
    val containerTabIgnored: Boolean?,
    @SerialName(FIELD_PINNED_TAB_ALLOWED)
    val pinnedTabAllowed: Boolean,
) {
    companion object {
        const val FIELD_IDLE_CONDITION = "idle_condition"
        const val FIELD_IDLE_TIMEOUT_IN_MINUTES = "idle_timeout_in_minutes"
        const val FIELD_MUTED_TAB_IGNORED = "muted_tab_ignored"
        const val FIELD_CONTAINER_TAB_IGNORED = "container_tab_ignored"
        const val FIELD_PINNED_TAB_ALLOWED = "pinned_tab_allowed"

        fun default() =
            StashRuleDocument(
                idleCondition = "window",
                idleTimeoutInMinutes = 60,
                mutedTabIgnored = false,
                containerTabIgnored = false,
                pinnedTabAllowed = false,
            )
    }
}
