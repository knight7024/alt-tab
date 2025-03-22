package com.example.domain.extension

interface BrowserTabInfoRepository {
    suspend fun save(browserTabInfo: BrowserTabInfo)
}
