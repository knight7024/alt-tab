package com.example.domain.extension

import java.time.Instant

data class BrowserTabInfo(
    val groupId: String? = null,
    val windowId: String,
    val tabIndex: Int,
    val title: String,
    val url: String,
    val faviconUrl: String?,
    val incognito: Boolean,
    val scrollPosition: RelativeRatio,
    val screenshot: String,
    val lastActiveAt: Instant,
)

data class RelativeRatio(
    val x: Double,
    val y: Double,
)
