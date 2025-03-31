package com.example.route.extension

import com.example.baseTestApplication
import com.example.bootstrapService
import com.example.clock
import com.example.hashIdCodec
import com.example.tabGroupRepository
import domain.extension.TabGroupFixtures
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import java.time.Duration

class TabGroupRouteTest :
    DescribeSpec({
        beforeTest {
            clock.reset()
        }

        describe("TabGroupRouteTest") {
            describe("나의 탭 그룹 조회") {
                val (user, accessToken, _) = bootstrapService.signUp()
                baseTestApplication(accessToken.value) { client ->
                    val response = client.get("/tab-group")

                    response.status shouldBe HttpStatusCode.OK
                    tabGroupRepository.findAllByUserId(user.id).also {
                        it.map { it.toDto() }.toSet() shouldBe response.body<Set<TabGroupDto>>()
                    }
                }
            }

            describe("탭 그룹 생성") {
                val (user, accessToken, _) = bootstrapService.signUp()
                baseTestApplication(accessToken.value) { client ->
                    val request = TabGroupFixtures.dummy(user.id).toDto()
                    val response =
                        client.post("/tab-group") {
                            setBody(request)
                        }
                    val id =
                        response
                            .body<CreateTabGroupResponse>()
                            .id
                            .let { hashIdCodec.decode(it) }
                            .also { it.isRight() shouldBe true }
                            .getOrNull()!!

                    response.status shouldBe HttpStatusCode.OK
                    tabGroupRepository.find(id).also {
                        it?.toDto() shouldBe request
                    }
                }
            }

            describe("탭 그룹 조회") {
                val (user, accessToken, _) = bootstrapService.signUp()
                baseTestApplication(accessToken.value) { client ->
                    val id =
                        client
                            .post("/tab-group") {
                                setBody(TabGroupFixtures.dummy(user.id).toDto())
                            }.body<CreateTabGroupResponse>()
                            .id
                            .let { hashIdCodec.decode(it) }
                            .also { it.isRight() shouldBe true }
                            .getOrNull()!!

                    val expiresId = hashIdCodec.encode(id, clock.instant().plusSeconds(1))

                    val response = client.get("/tab-group/$expiresId")

                    response.status shouldBe HttpStatusCode.OK
                    tabGroupRepository.find(id).also {
                        it?.toDto() shouldBe response.body<TabGroupDto>()
                    }
                }
            }

            describe("만료된 QR 코드로 탭 그룹 조회") {
                val (user, accessToken, _) = bootstrapService.signUp()
                baseTestApplication(accessToken.value) { client ->
                    val id =
                        client
                            .post("/tab-group") {
                                setBody(TabGroupFixtures.dummy(user.id).toDto())
                            }.body<CreateTabGroupResponse>()
                            .id
                            .let { hashIdCodec.decode(it) }
                            .also { it.isRight() shouldBe true }
                            .getOrNull()!!

                    val expiresId = hashIdCodec.encode(id, clock.instant().plusSeconds(1))
                    clock.tick(Duration.ofSeconds(1))

                    val response = client.get("/tab-group/$expiresId")

                    response.status shouldBe HttpStatusCode.NotFound
                }
            }

            describe("존재하지 않는 탭 그룹 조회") {
                baseTestApplication { client ->
                    val id = hashIdCodec.encode(0L, clock.instant().plusSeconds(1))

                    val response = client.get("/tab-group/$id")

                    response.status shouldBe HttpStatusCode.NotFound
                }
            }
        }
    })
