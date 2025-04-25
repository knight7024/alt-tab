package com.example.route.auth

import com.example.adapter.rabbitmq.service.InvalidateRefreshTokenMessage
import com.example.domain.SendAsyncMessage
import com.example.domain.extension.StashSettingRepository
import com.example.domain.token.RefreshTokenRepository
import com.example.domain.token.TokenId
import com.example.domain.token.TokenProvider
import com.example.domain.token.TokenValidator
import com.example.domain.user.UserAuthorizationService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Routing
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.VisibleForTesting

fun Routing.authorization(
    userAuthorizationService: UserAuthorizationService,
    stashSettingRepository: StashSettingRepository,
    tokenProvider: TokenProvider,
    tokenValidator: TokenValidator,
    refreshTokenRepository: RefreshTokenRepository,
    invalidateRefreshToken: SendAsyncMessage,
) {
    route("/oauth") {
        authenticate("auth-oauth-google") {
            get("/google") {
                val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()!!
                val user = userAuthorizationService.byGoogleOAuth(principal.accessToken)

                val (accessToken, refreshToken) =
                    tokenProvider
                        .issueAll(TokenId(user.id))
                        .also {
                            refreshTokenRepository.save(it.second)
                            stashSettingRepository.init(user.id)
                        }

                return@get call.respondRedirect("/oauth/complete#access_token=${accessToken.value}&refresh_token=${refreshToken.value}")
            }
        }

        // 브라우저 익스텐션에서 팝업으로 띄워진 창의 응답을 확인하기 위해 앵커(#)로 토큰 값을 담아준다.
        get("/complete") {
            return@get call.respond(HttpStatusCode.OK)
        }
    }

    post("/refresh-tokens") {
        val tokens = call.receive<TokenDto>()
        tokenValidator
            .validate(tokens.accessToken, tokens.refreshToken)
            .onLeft { error ->
                when (error) {
                    is TokenValidator.Error.AccessTokenExpired -> {
                        // 발급보다 먼저 만료시켜야 토큰 탈취에 보다 안전하다.
                        // 에러가 발생해도 사용성에 문제 없는 편이 낫다.
                        runCatching {
                            val stolen = !refreshTokenRepository.invalidateOnce(error.refreshToken)
                            if (stolen) {
                                application.environment.log.warn("refresh token stolen: ${error.refreshToken.value}")
                                invalidateRefreshToken.invoke(
                                    InvalidateRefreshTokenMessage(
                                        userId = error.refreshToken.tokenId.userId.value,
                                        pairingKey = error.refreshToken.tokenId.pairingKey,
                                    ),
                                )
                                return@post call.respond(HttpStatusCode.Unauthorized)
                            }
                        }.onFailure {
                            application.environment.log.error(
                                "error occurred while invalidating refresh token",
                                it,
                            )
                        }

                        val (accessToken, refreshToken) =
                            tokenProvider
                                .issueAll(error.refreshToken.tokenId)
                                .also { refreshTokenRepository.save(it.second) }

                        return@post call.respond(TokenDto(accessToken.value, refreshToken.value))
                    }

                    else -> {
                        return@post call.respond(HttpStatusCode.Unauthorized)
                    }
                }
            }.onRight { (accessToken, refreshToken) ->
                return@post call.respond(TokenDto(accessToken.value, refreshToken.value))
            }
    }
}

@VisibleForTesting
@Serializable
internal data class TokenDto(
    val accessToken: String,
    val refreshToken: String,
)
