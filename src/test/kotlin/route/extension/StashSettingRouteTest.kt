package com.example.route.extension

import com.example.baseTestApplication
import com.example.bootstrapService
import com.example.stashSettingRepository
import domain.extension.StashSettingFixtures
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode

class StashSettingRouteTest :
    DescribeSpec({
        describe("StashSettingRouteTest") {
            val (user, accessToken, _) = bootstrapService.signUp()

            describe("내 스태시 설정 조회") {
                baseTestApplication(accessToken.value) { client ->
                    val response = client.get("/stash-setting")

                    response.status shouldBe HttpStatusCode.OK
                    stashSettingRepository.find(user.id).also {
                        it.toDto() shouldBe response.body<StashSettingDto>()
                    }
                }
            }

            describe("내 스태시 설정 업데이트") {
                baseTestApplication(accessToken.value) { client ->
                    val asIsSetting = stashSettingRepository.find(user.id)
                    val toBeSetting = StashSettingFixtures.dummyStashSetting(user.id)
                    val response =
                        client.put("/stash-setting/update") {
                            setBody(toBeSetting.toDto())
                        }

                    response.status shouldBe HttpStatusCode.OK
                    stashSettingRepository.find(user.id).also {
                        asIsSetting shouldNotBe it
                        toBeSetting shouldBe it
                    }
                }
            }
        }
    })
