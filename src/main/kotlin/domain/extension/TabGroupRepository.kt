package com.example.domain.extension

import com.example.domain.user.UserId

interface TabGroupRepository {
    suspend fun find(id: Long): TabGroup

    suspend fun save(
        userId: UserId,
        secret: String,
        salt: String,
        tabs: Collection<BrowserTabInfo>,
    )
}
