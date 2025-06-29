package com.example.route.auth

import com.example.adapter.rabbitmq.service.InvalidateRefreshTokenMessage
import com.example.baseTestApplication
import com.example.bootstrapService
import com.example.clock
import com.example.domain.token.AccessToken
import com.example.domain.token.RefreshToken
import com.example.refreshTokenRepository
import com.example.sendAsyncMessage
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.mockk.coVerify
import java.util.UUID

class AuthRouteTest :
    DescribeSpec({
        beforeTest {
            clock.reset()
        }

        describe("AuthRouteTest") {
            describe("만료되지 않은 액세스 토큰과 리프레시 토큰") {
                baseTestApplication { client ->
                    val (_, accessToken, refreshToken) = bootstrapService.signUp()

                    val response =
                        client.post("/refresh-tokens") {
                            setBody(TokenDto(accessToken.value, refreshToken.value))
                        }

                    response.status shouldBe HttpStatusCode.OK
                    response.body<TokenDto>().apply {
                        this.accessToken shouldBe accessToken.value
                        this.refreshToken shouldBe refreshToken.value
                    }
                }
            }

            describe("만료된 액세스 토큰과 만료되지 않은 리프레시 토큰") {
                baseTestApplication { client ->
                    clock.tick(AccessToken.EXPIRES_IN.plusMinutes(1).negated())
                    val (_, accessToken, refreshToken) = bootstrapService.signUp()
                    clock.reset()

                    val response =
                        client.post("/refresh-tokens") {
                            setBody(TokenDto(accessToken.value, refreshToken.value))
                        }

                    response.status shouldBe HttpStatusCode.OK
                    response.body<TokenDto>().apply {
                        this.accessToken shouldNotBe accessToken.value
                        this.refreshToken shouldNotBe refreshToken.value
                    }
                }
            }

            describe("만료된 액세스 토큰과 만료된 리프레시 토큰") {
                baseTestApplication { client ->
                    val (_, accessToken, refreshToken) = bootstrapService.signUp()
                    clock.tick(RefreshToken.EXPIRES_IN)

                    val response =
                        client.post("/refresh-tokens") {
                            setBody(TokenDto(accessToken.value, refreshToken.value))
                        }

                    response.status shouldBe HttpStatusCode.Unauthorized
                }
            }

            describe("사용자가 정상 토큰으로 갱신 이후 어뷰저가 동일한 토큰으로 재갱신") {
                baseTestApplication { client ->
                    clock.tick(AccessToken.EXPIRES_IN.plusMinutes(1).negated())
                    val (_, accessToken, refreshToken) = bootstrapService.signUp()
                    clock.reset()

                    val normalUserResponse =
                        client.post("/refresh-tokens") {
                            setBody(TokenDto(accessToken.value, refreshToken.value))
                        }

                    val abuserResponse =
                        client.post("/refresh-tokens") {
                            setBody(TokenDto(accessToken.value, refreshToken.value))
                        }
                    // simulating token invalidation
                    refreshTokenRepository.invalidateAll(accessToken.tokenId)

                    normalUserResponse.status shouldBe HttpStatusCode.OK
                    normalUserResponse.body<TokenDto>().apply {
                        this.accessToken shouldNotBe accessToken.value
                        this.refreshToken shouldNotBe refreshToken.value
                    }

                    abuserResponse.status shouldBe HttpStatusCode.Unauthorized
                    coVerify {
                        sendAsyncMessage(
                            InvalidateRefreshTokenMessage(
                                accessToken.tokenId.userId.value,
                                accessToken.tokenId.pairingKey,
                            ),
                        )
                    }
                }
            }

            describe("탈취된 토큰으로 판단되어 제거된 토큰으로 재갱신하는 경우") {
                baseTestApplication { client ->
                    clock.tick(AccessToken.EXPIRES_IN.plusMinutes(1).negated())
                    val (_, accessToken, refreshToken) = bootstrapService.signUp()
                    clock.reset()

                    val normalUserResponse =
                        client.post("/refresh-tokens") {
                            setBody(TokenDto(accessToken.value, refreshToken.value))
                        }

                    val abuserResponseBeforePurged =
                        client.post("/refresh-tokens") {
                            setBody(TokenDto(accessToken.value, refreshToken.value))
                        }
                    // simulating token invalidation
                    refreshTokenRepository.invalidateAll(accessToken.tokenId)

                    val abuserResponseAfterPurged =
                        client.post("/refresh-tokens") {
                            setBody(TokenDto(accessToken.value, refreshToken.value))
                        }

                    normalUserResponse.status shouldBe HttpStatusCode.OK
                    normalUserResponse.body<TokenDto>().apply {
                        this.accessToken shouldNotBe accessToken.value
                        this.refreshToken shouldNotBe refreshToken.value
                    }

                    abuserResponseBeforePurged.status shouldBe HttpStatusCode.Unauthorized
                    abuserResponseAfterPurged.status shouldBe HttpStatusCode.Unauthorized
                    coVerify(exactly = 1) {
                        sendAsyncMessage(
                            InvalidateRefreshTokenMessage(
                                accessToken.tokenId.userId.value,
                                accessToken.tokenId.pairingKey,
                            ),
                        )
                    }
                }
            }

            describe("잘못된 액세스 토큰과 리프레시 토큰") {
                baseTestApplication { client ->
                    val invalid = UUID.randomUUID().toString()

                    val response =
                        client.post("/refresh-tokens") {
                            setBody(TokenDto(invalid, invalid))
                        }

                    response.status shouldBe HttpStatusCode.Unauthorized
                }
            }
        }
    })
