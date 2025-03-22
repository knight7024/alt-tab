package com.example.domain.extension

import com.example.domain.user.UserId

interface StashSettingRepository {
    suspend fun init(userId: UserId)

    suspend fun update(stashSetting: StashSetting)

    suspend fun find(userId: UserId): StashSetting
}
