package com.example.domain.extension

import com.example.domain.user.UserId

data class Device(
    val id: String,
    val nickname: String,
    val userAgent: String,
)

data class DeviceSetting(
    val userId: UserId,
    val deviceId: String,
    val stashSetting: StashSetting,
)

data class StashSetting(
    val whitelistUrls: List<String>,
)
