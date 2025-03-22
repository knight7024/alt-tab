package adapter

import com.example.adapter.StashRuleDocument
import com.example.adapter.StashSettingDocument
import com.example.domain.extension.StashRule
import com.example.domain.extension.StashSetting
import com.example.domain.extension.StashSettingRepository
import com.example.domain.user.UserId
import java.time.Duration

class FakeStashSettingRepository : StashSettingRepository {
    private val stashSettingByUserId = mutableMapOf<String, StashSettingDocument>()

    override suspend fun init(userId: UserId) {
        stashSettingByUserId[userId.value] =
            StashSettingDocument(
                userId = userId.value,
                globalRule = StashRuleDocument.default(),
                whitelistUrls = null,
            )
    }

    override suspend fun update(stashSetting: StashSetting) {
        stashSettingByUserId[stashSetting.userId.value] = stashSetting.toDocument()
    }

    override suspend fun find(userId: UserId): StashSetting = stashSettingByUserId[userId.value]!!.toDomain()

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
