package com.example.route

import arrow.core.right
import com.example.baseTestApplication
import com.example.domain.token.TokenValidator
import com.example.route.auth.TokenDto
import domain.token.AccessTokenFixtures
import domain.token.RefreshTokenFixtures
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import java.util.UUID

class AuthRouteTest :
    BehaviorSpec({
        // mock
        val tokenValidator = mockk<TokenValidator>()

        afterSpec {
            confirmVerified(tokenValidator)
        }

        given("good - 만료되지 않은 액세스 토큰과 리프레시 토큰") {
            baseTestApplication { client ->
                val accessToken = UUID.randomUUID().toString()
                val refreshToken = UUID.randomUUID().toString()
                every { tokenValidator.validate(accessToken, refreshToken) } returns
                    (AccessTokenFixtures.dummy(accessToken) to RefreshTokenFixtures.dummy(refreshToken)).right()

                val response =
                    client.post("/refresh-tokens") {
                        setBody(TokenDto(accessToken, refreshToken))
                    }

                then("그대로 돌려준다.") {
                    response.status shouldBe HttpStatusCode.OK
                    response.body<TokenDto>().apply {
                        accessToken shouldBe accessToken
                        refreshToken shouldBe refreshToken
                    }
                }
            }
        }
    })
