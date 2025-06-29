package com.example.module

import com.example.domain.SendAsyncMessage
import com.example.domain.extension.StashSettingRepository
import com.example.domain.extension.TabGroupRepository
import com.example.domain.token.RefreshTokenRepository
import com.example.domain.token.TokenProvider
import com.example.domain.token.TokenValidator
import com.example.domain.user.UserAuthorizationService
import com.example.route.auth.authorization
import com.example.route.extension.stashSetting
import com.example.route.extension.tabGroup
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.time.Clock

internal fun Application.configureRouting(
    userAuthorizationService: UserAuthorizationService,
    stashSettingRepository: StashSettingRepository,
    tokenProvider: TokenProvider,
    tokenValidator: TokenValidator,
    refreshTokenRepository: RefreshTokenRepository,
    sendAsyncMessage: SendAsyncMessage,
    tabGroupRepository: TabGroupRepository,
    clock: Clock,
) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        authorization(
            userAuthorizationService = userAuthorizationService,
            stashSettingRepository = stashSettingRepository,
            tokenProvider = tokenProvider,
            tokenValidator = tokenValidator,
            refreshTokenRepository = refreshTokenRepository,
            sendAsyncMessage = sendAsyncMessage,
        )

        stashSetting(
            stashSettingRepository = stashSettingRepository,
        )

        tabGroup(
            tabGroupRepository = tabGroupRepository,
            clock = clock,
        )
    }
}
