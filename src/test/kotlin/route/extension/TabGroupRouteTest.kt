package com.example.route.extension

import com.example.baseTestApplication
import com.example.bootstrapService
import com.example.clock
import com.example.hashIdCodec
import com.example.tabGroupRepository
import domain.extension.TabGroupFixtures
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.request.delete
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
                    val tabGroup = TabGroupFixtures.dummy(userId = user.id)
                    val response =
                        client.post("/tab-group") {
                            setBody(
                                CreateTabGroupRequest(
                                    secret = tabGroup.secret,
                                    salt = tabGroup.salt,
                                    browserTabInfos = tabGroup.tabs.map { it.toDto() },
                                ),
                            )
                        }
                    val id = response.body<CreateTabGroupResponse>().id

                    response.status shouldBe HttpStatusCode.OK
                    tabGroupRepository.find(id).also {
                        it shouldBe tabGroup.copy(id = id)
                    }
                }
            }

            describe("탭 그룹 조회") {
                val (user, accessToken, _) = bootstrapService.signUp()
                baseTestApplication(accessToken.value) { client ->
                    // given
                    val tabGroup = TabGroupFixtures.dummy(userId = user.id)
                    val id =
                        client
                            .post("/tab-group") {
                                setBody(
                                    CreateTabGroupRequest(
                                        secret = tabGroup.secret,
                                        salt = tabGroup.salt,
                                        browserTabInfos = tabGroup.tabs.map { it.toDto() },
                                    ),
                                )
                            }.body<CreateTabGroupResponse>()
                            .id

                    // when
                    val response = client.get("/tab-group/$id")

                    // then
                    response.status shouldBe HttpStatusCode.OK
                    tabGroupRepository.find(id).also {
                        it?.toDto() shouldBe response.body<TabGroupDto>()
                    }
                }
            }

            describe("만료된 qr 코드로 탭 그룹 조회") {
                val (user, accessToken, _) = bootstrapService.signUp()
                baseTestApplication(accessToken.value) { client ->
                    // given
                    val tabGroup = TabGroupFixtures.dummy(userId = user.id)
                    val id =
                        client
                            .post("/tab-group") {
                                setBody(
                                    CreateTabGroupRequest(
                                        secret = tabGroup.secret,
                                        salt = tabGroup.salt,
                                        browserTabInfos = tabGroup.tabs.map { it.toDto() },
                                    ),
                                )
                            }.body<CreateTabGroupResponse>()
                            .id
                            .let { hashIdCodec.decode(it) }
                            .also { it.isRight() shouldBe true }
                            .getOrNull()!!

                    val expiresId = hashIdCodec.encode(id, clock.instant().plusSeconds(1))
                    clock.tick(Duration.ofSeconds(1))

                    // when
                    val response = client.get("/tab-group/$expiresId")

                    // then
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

            describe("나의 탭 그룹 삭제") {
                val (user, accessToken, _) = bootstrapService.signUp()
                baseTestApplication(accessToken.value) { client ->
                    // given
                    val tabGroup = TabGroupFixtures.dummy(userId = user.id)
                    val id =
                        client
                            .post("/tab-group") {
                                setBody(
                                    CreateTabGroupRequest(
                                        secret = tabGroup.secret,
                                        salt = tabGroup.salt,
                                        browserTabInfos = tabGroup.tabs.map { it.toDto() },
                                    ),
                                )
                            }.body<CreateTabGroupResponse>()
                            .id

                    // when
                    val response =
                        client.delete("/tab-group") {
                            setBody(DeleteTabGroupRequest(id = id))
                        }

                    response.status shouldBe HttpStatusCode.OK
                    tabGroupRepository.find(id).also {
                        it.shouldBeNull()
                    }
                }
            }

            describe("남의 탭 그룹 삭제") {
                val (user1, accessToken1, _) = bootstrapService.signUp()
                val (_, accessToken2, _) = bootstrapService.signUp()
                baseTestApplication(accessToken1.value) { client ->
                    // given
                    val tabGroup = TabGroupFixtures.dummy(userId = user1.id)
                    client
                        .post("/tab-group") {
                            setBody(
                                CreateTabGroupRequest(
                                    secret = tabGroup.secret,
                                    salt = tabGroup.salt,
                                    browserTabInfos = tabGroup.tabs.map { it.toDto() },
                                ),
                            )
                        }
                }
                baseTestApplication(accessToken2.value) { client ->
                    val id = tabGroupRepository.findAllByUserId(user1.id).first().id
                    // when
                    val response =
                        client.delete("/tab-group") {
                            setBody(DeleteTabGroupRequest(id = id))
                        }

                    // then
                    response.status shouldBe HttpStatusCode.InternalServerError
                    tabGroupRepository.find(id).also {
                        it.shouldNotBeNull()
                    }
                }
            }

            describe("나의 탭 그룹으로 QR 코드 발급") {
                val (user, accessToken, _) = bootstrapService.signUp()
                baseTestApplication(accessToken.value) { client ->
                    // given
                    val tabGroup = TabGroupFixtures.dummy(userId = user.id)
                    val id =
                        client
                            .post("/tab-group") {
                                setBody(
                                    CreateTabGroupRequest(
                                        secret = tabGroup.secret,
                                        salt = tabGroup.salt,
                                        browserTabInfos = tabGroup.tabs.map { it.toDto() },
                                    ),
                                )
                            }.body<CreateTabGroupResponse>()
                            .id

                    // when
                    val response =
                        client.post("/tab-group/qr-code") {
                            setBody(
                                CreateTabGroupQrCodeRequest(
                                    id = id,
                                    alive = 600,
                                ),
                            )
                        }

                    // then
                    response.status shouldBe HttpStatusCode.OK
                    hashIdCodec.decode(id).getOrNull()!!.also {
                        val qrCodeId = hashIdCodec.encode(it, clock.instant() + Duration.ofSeconds(600))
                        response.body<CreateTabGroupQrCodeResponse>().path shouldBe "/tab-group/$qrCodeId"
                    }
                }
            }

            describe("남의 탭 그룹으로 QR 코드 발급") {
                val (user1, accessToken1, _) = bootstrapService.signUp()
                val (_, accessToken2, _) = bootstrapService.signUp()
                baseTestApplication(accessToken1.value) { client ->
                    // given
                    val tabGroup = TabGroupFixtures.dummy(userId = user1.id)
                    client
                        .post("/tab-group") {
                            setBody(
                                CreateTabGroupRequest(
                                    secret = tabGroup.secret,
                                    salt = tabGroup.salt,
                                    browserTabInfos = tabGroup.tabs.map { it.toDto() },
                                ),
                            )
                        }
                }
                baseTestApplication(accessToken2.value) { client ->
                    val id = tabGroupRepository.findAllByUserId(user1.id).first().id
                    // when
                    val response =
                        client.post("/tab-group/qr-code") {
                            setBody(
                                CreateTabGroupQrCodeRequest(
                                    id = id,
                                    alive = 600,
                                ),
                            )
                        }

                    // then
                    response.status shouldBe HttpStatusCode.InternalServerError
                }
            }
        }
    })
