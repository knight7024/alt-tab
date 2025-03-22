package com.example.route.auth

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
import io.ktor.server.routing.Routing
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

                return@get call.respond(TokenDto(accessToken.value, refreshToken.value))
            }
        }
    }

    post("/refresh-tokens") {
        val tokens = call.receive<TokenDto>()
        tokenValidator
            .validate(tokens.accessToken, tokens.refreshToken)
            .onLeft {
                when (it) {
                    is TokenValidator.Error.AccessTokenExpired -> {
                        // 발급보다 먼저 만료시켜야 토큰 탈취에 보다 안전하다.
                        // 에러가 발생해도 사용성에 문제 없는 편이 낫다.
                        runCatching {
                            val stolen = !refreshTokenRepository.invalidateOnce(it.refreshToken)
                            if (stolen) {
                                // TODO: 연관된 모든 토큰 만료시켜야 한다.
                                return@post call.respond(HttpStatusCode.Unauthorized)
                            }
                        }

                        val (accessToken, refreshToken) =
                            tokenProvider
                                .issueAll(it.refreshToken.tokenId)
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
