package com.example.domain.extension

import com.example.domain.user.UserId
import java.time.Duration

/**
 * @param userId 유저 id
 * @param globalRule 글로벌 규칙
 * @param whitelistUrls 글로벌 규칙을 따르지 않는 예외
 */
data class StashSetting(
    val userId: UserId,
    val globalRule: StashRule,
    val whitelistUrls: Map<String, StashRule?>,
)

data class StashRule(
    val idleCondition: String,
    val idleTimeout: Duration,
    val mutedTabIgnored: Boolean,
    val containerTabIgnored: Boolean?,
    val pinnedTabAllowed: Boolean,
)
