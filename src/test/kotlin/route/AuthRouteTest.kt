package com.example.route

import com.example.baseTestApplication
import com.example.clock
import com.example.domain.token.AccessToken
import com.example.domain.token.RefreshToken
import com.example.route.auth.TokenDto
import com.example.tokenProvider
import domain.token.TokenIdFixtures
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode

class AuthRouteTest :
    DescribeSpec({
        describe("AuthRouteTest") {
            describe("만료되지 않은 액세스 토큰과 리프레시 토큰") {
                baseTestApplication { client ->
                    val tokenId = TokenIdFixtures.dummy()
                    val (accessToken, refreshToken) = tokenProvider.issueAll(tokenId)

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
                    val tokenId = TokenIdFixtures.dummy()
                    val (accessToken, refreshToken) = tokenProvider.issueAll(tokenId)
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
                    val tokenId = TokenIdFixtures.dummy()
                    val (accessToken, refreshToken) = tokenProvider.issueAll(tokenId)
                    clock.tick(RefreshToken.EXPIRES_IN)

                    val response =
                        client.post("/refresh-tokens") {
                            setBody(TokenDto(accessToken.value, refreshToken.value))
                        }

                    response.status shouldBe HttpStatusCode.Unauthorized
                }
            }
        }
    })
