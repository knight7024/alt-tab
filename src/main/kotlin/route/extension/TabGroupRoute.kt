package com.example.route.extension

import arrow.core.getOrElse
import com.example.domain.extension.BrowserTabInfo
import com.example.domain.extension.HashIdCodec
import com.example.domain.extension.RelativeRatio
import com.example.domain.extension.TabGroup
import com.example.domain.extension.TabGroupRepository
import com.example.module.authenticatedUser
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.AuthenticationStrategy
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.VisibleForTesting
import java.time.Instant

fun Routing.tabGroup(
    tabGroupRepository: TabGroupRepository,
    hashIdCodec: HashIdCodec,
) {
    authenticate("auth-bearer", strategy = AuthenticationStrategy.Required) {
        route("/tab-group") {
            get {
                val user = call.authenticatedUser()
                val tabGroups =
                    tabGroupRepository
                        .findAllByUserId(user.id)
                        .map { it.toDto() }

                return@get call.respond(tabGroups)
            }

            post {
                val user = call.authenticatedUser()
                val tabGroup = call.receive<TabGroupDto>()

                val tabGroupId =
                    tabGroupRepository
                        .save(
                            userId = user.id,
                            secret = tabGroup.secret,
                            salt = tabGroup.salt,
                            tabs = tabGroup.browserTabInfos.map { it.toDomain() },
                        ).let {
                            hashIdCodec.encode(it)
                        }

                return@post call.respond(CreateTabGroupResponse(tabGroupId))
            }

            // TODO: QR 발급 API
        }
    }

    // QR 코드를 통해 접근하는 경우라서 인증이 필요 없다.
    get("/tab-group/{id}") {
        val id =
            call.parameters["id"]!!
                .let { hashIdCodec.decode(it) }
                .getOrElse { return@get call.respond(HttpStatusCode.NotFound) }

        val tabGroup =
            tabGroupRepository.find(id)
                ?: return@get call.respond(HttpStatusCode.NotFound)

        return@get call.respond(tabGroup.toDto())
    }
}

@VisibleForTesting
internal fun TabGroup.toDto() =
    TabGroupDto(
        secret = secret,
        salt = salt,
        browserTabInfos = tabs.map { it.toDto() },
    )

private fun BrowserTabInfo.toDto() =
    BrowserTabInfoDto(
        windowId = windowId,
        groupId = groupId,
        tabIndex = tabIndex,
        title = title,
        url = url,
        faviconUrl = faviconUrl,
        incognito = incognito,
        scrollPosition =
            BrowserTabInfoDto.RelativeRatio(
                x = scrollPosition.x,
                y = scrollPosition.y,
            ),
        lastUsedAgent = lastUsedAgent,
        lastActiveAt = lastActiveAt.epochSecond,
        session = session,
        cookie = cookie,
    )

private fun BrowserTabInfoDto.toDomain() =
    BrowserTabInfo(
        windowId = windowId,
        groupId = groupId,
        tabIndex = tabIndex,
        title = title,
        url = url,
        faviconUrl = faviconUrl,
        incognito = incognito,
        scrollPosition =
            RelativeRatio(
                x = scrollPosition.x,
                y = scrollPosition.y,
            ),
        lastUsedAgent = lastUsedAgent,
        lastActiveAt = Instant.ofEpochSecond(lastActiveAt),
        session = session,
        cookie = cookie,
    )

@Serializable
internal data class TabGroupDto(
    val secret: String,
    val salt: String,
    val browserTabInfos: List<BrowserTabInfoDto>,
)

@Serializable
internal data class BrowserTabInfoDto(
    val windowId: String,
    val groupId: String?,
    val tabIndex: Int,
    val title: String,
    val url: String,
    val faviconUrl: String?,
    val incognito: Boolean,
    val scrollPosition: RelativeRatio,
    val lastUsedAgent: String,
    val lastActiveAt: Long,
    val session: String,
    val cookie: String,
) {
    @Serializable
    data class RelativeRatio(
        val x: Double,
        val y: Double,
    )
}

@Serializable
internal data class CreateTabGroupResponse(
    val id: String,
)
