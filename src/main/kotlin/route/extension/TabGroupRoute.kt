package com.example.route.extension

import com.example.domain.extension.BrowserTabInfo
import com.example.domain.extension.HashIdCodec
import com.example.domain.extension.QRExpiry
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
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.VisibleForTesting
import java.time.Clock
import java.time.Instant

fun Routing.tabGroup(
    tabGroupRepository: TabGroupRepository,
    hashIdCodec: HashIdCodec,
    clock: Clock,
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
                val tabGroup = call.receive<CreateTabGroupRequest>()

                val tabGroupId =
                    tabGroupRepository
                        .save(
                            userId = user.id,
                            secret = tabGroup.secret,
                            salt = tabGroup.salt,
                            tabs = tabGroup.browserTabInfos.map { it.toDomain() },
                        )

                return@post call.respond(CreateTabGroupResponse(tabGroupId))
            }

            delete {
                val user = call.authenticatedUser()
                val tabGroup =
                    call
                        .receive<DeleteTabGroupRequest>()
                        .id
                        .let { tabGroupRepository.find(it) }

                checkNotNull(tabGroup)
                check(tabGroup.userId == user.id)

                tabGroupRepository.remove(tabGroup.id)

                return@delete call.respond(HttpStatusCode.OK)
            }

            post("/qr-code") {
                val user = call.authenticatedUser()
                val request = call.receive<CreateTabGroupQrCodeRequest>()
                val qrExpiry = QRExpiry.of(request.alive)

                val tabGroup =
                    tabGroupRepository.find(request.id)
                        ?: return@post call.respond(HttpStatusCode.NotFound)

                check(tabGroup.userId == user.id)

                val numericId = hashIdCodec.decode(tabGroup.id).getOrNull()!!
                val qrCodeId = hashIdCodec.encode(numericId, clock.instant() + qrExpiry.duration)

                return@post call.respond(CreateTabGroupQrCodeResponse("/tab-group/$qrCodeId"))
            }
        }
    }

    // QR 코드를 통해 접근하는 경우라서 인증이 필요 없다.
    get("/tab-group/{id}") {
        val id = call.parameters["id"]!!

        val tabGroup =
            tabGroupRepository.find(id)
                ?: return@get call.respond(HttpStatusCode.NotFound)

        return@get call.respond(tabGroup.toDto())
    }
}

@VisibleForTesting
internal fun TabGroup.toDto() =
    TabGroupDto(
        id = id,
        secret = secret,
        salt = salt,
        browserTabInfos = tabs.map { it.toDto() },
    )

@Serializable
internal data class TabGroupDto(
    val id: String,
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

@VisibleForTesting
internal fun BrowserTabInfo.toDto() =
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
internal data class CreateTabGroupRequest(
    val secret: String,
    val salt: String,
    val browserTabInfos: List<BrowserTabInfoDto>,
)

@Serializable
internal data class CreateTabGroupResponse(
    val id: String,
)

@Serializable
internal data class DeleteTabGroupRequest(
    val id: String,
)

@Serializable
internal data class CreateTabGroupQrCodeRequest(
    val id: String,
    val alive: Long,
)

@Serializable
internal data class CreateTabGroupQrCodeResponse(
    val path: String,
)
