package com.example.route.extension

import com.example.adapter.MongoBrowserTabInfoRepository
import io.ktor.server.auth.AuthenticationStrategy
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Routing

fun Routing.browserTabInfo(browserTabInfoRepository: MongoBrowserTabInfoRepository) {
    authenticate("auth-bearer", strategy = AuthenticationStrategy.Required) {
    }
}
