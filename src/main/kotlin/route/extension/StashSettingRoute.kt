package com.example.route.extension

import com.example.domain.extension.StashRule
import com.example.domain.extension.StashSetting
import com.example.domain.extension.StashSettingRepository
import com.example.module.authenticatedUser
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.AuthenticationStrategy
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.VisibleForTesting
import java.time.Duration

fun Routing.stashSetting(stashSettingRepository: StashSettingRepository) {
    authenticate("auth-bearer", strategy = AuthenticationStrategy.Required) {
        route("/stash-setting") {
            get {
                val user = call.authenticatedUser()
                val stashSetting = stashSettingRepository.find(user.id)

                return@get call.respond(HttpStatusCode.OK, stashSetting.toDto())
            }

            put("/update") {
                val user = call.authenticatedUser()
                val toBeSetting = call.receive<StashSettingDto>()

                stashSettingRepository.update(
                    StashSetting(
                        userId = user.id,
                        globalRule = toBeSetting.globalRule.toDomain(),
                        whitelistUrls = toBeSetting.whitelistUrls.mapValues { it.value?.toDomain() },
                    ),
                )

                return@put call.respond(HttpStatusCode.OK)
            }
        }
    }
}

@VisibleForTesting
internal fun StashSetting.toDto() =
    StashSettingDto(
        globalRule = globalRule.toDto(),
        whitelistUrls = whitelistUrls.mapValues { it.value?.toDto() },
    )

@VisibleForTesting
internal fun StashRule.toDto() =
    StashSettingDto.StashRule(
        idleCondition = idleCondition,
        idleTimeout = idleTimeout.toMinutes().toInt(),
        ignoreUnloadedTab = ignoreUnloadedTab,
        ignoreContainerTab = ignoreContainerTab,
        allowPinnedTab = allowPinnedTab,
        ignoreAudibleTab = ignoreAudibleTab,
    )

private fun StashSettingDto.StashRule.toDomain() =
    StashRule(
        idleCondition = idleCondition,
        idleTimeout = Duration.ofMinutes(idleTimeout.toLong()),
        ignoreUnloadedTab = ignoreUnloadedTab,
        ignoreContainerTab = ignoreContainerTab,
        allowPinnedTab = allowPinnedTab,
        ignoreAudibleTab = ignoreAudibleTab,
    )

@VisibleForTesting
@Serializable
internal data class StashSettingDto(
    val globalRule: StashRule,
    val whitelistUrls: Map<String, StashRule?>,
) {
    @Serializable
    data class StashRule(
        val idleCondition: String,
        val idleTimeout: Int,
        val ignoreUnloadedTab: Boolean,
        val ignoreAudibleTab: Boolean,
        val ignoreContainerTab: Boolean? = null,
        val allowPinnedTab: Boolean,
    ) {
        init {
            require(idleTimeout in 10..60)
        }
    }
}
