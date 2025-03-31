package com.example.domain.extension

import com.example.domain.user.UserId

interface TabGroupRepository {
    suspend fun find(id: Long): TabGroup?

    /**
     * @return 저장된 탭 그룹 id
     */
    suspend fun save(
        userId: UserId,
        secret: String,
        salt: String,
        tabs: Collection<BrowserTabInfo>,
    ): Long
}
