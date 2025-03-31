package com.example.route.extension

import com.example.baseTestApplication
import com.example.bootstrapService
import com.example.clock
import com.example.domain.extension.HashIdCodec
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

                    val numericId = tabGroupRepository.findAllByUserId(user.id).first().id

                    response.status shouldBe HttpStatusCode.OK
                    tabGroupRepository.find(numericId).also {
                        it shouldBe tabGroup.copy(id = numericId)
                    }
                }
            }

            describe("나의 탭 그룹 삭제") {
                val (user, accessToken, _) = bootstrapService.signUp()
                baseTestApplication(accessToken.value) { client ->
                    // given
                    val tabGroup = TabGroupFixtures.dummy(userId = user.id)
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

                    val numericId = tabGroupRepository.findAllByUserId(user.id).first().id

                    // when
                    val response =
                        client.delete("/tab-group") {
                            setBody(DeleteTabGroupRequest(id = HashIdCodec.encode(numericId)))
                        }

                    response.status shouldBe HttpStatusCode.OK
                    tabGroupRepository.find(numericId).also {
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
                    val numericId = tabGroupRepository.findAllByUserId(user1.id).first().id
                    // when
                    val response =
                        client.delete("/tab-group") {
                            setBody(DeleteTabGroupRequest(id = HashIdCodec.encode(numericId)))
                        }

                    // then
                    response.status shouldBe HttpStatusCode.InternalServerError
                    tabGroupRepository.find(numericId).also {
                        it.shouldNotBeNull()
                    }
                }
            }

            describe("나의 탭 그룹으로 QR 코드 발급") {
                val (user, accessToken, _) = bootstrapService.signUp()
                baseTestApplication(accessToken.value) { client ->
                    // given
                    val tabGroup = TabGroupFixtures.dummy(userId = user.id)
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
                    val id = HashIdCodec.encode(tabGroupRepository.findAllByUserId(user.id).first().id)

                    // when
                    val response =
                        client.post("/tab-group/qr-code") {
                            setBody(
                                CreateTabGroupQrCodeRequest(
                                    id = id,
                                ),
                            )
                        }

                    // then
                    response.status shouldBe HttpStatusCode.OK
                    HashIdCodec.decode(id).also {
                        val qrCodeId = HashIdCodec.encode(it.first(), clock.instant().plusSeconds(600))
                        response.body<CreateTabGroupQrCodeResponse>().path shouldBe "/tab-group/$qrCodeId"
                    }
                }
            }

            describe("QR 코드로 탭 그룹 조회") {
                val (user, accessToken, _) = bootstrapService.signUp()
                baseTestApplication(accessToken.value) { client ->
                    // given
                    val tabGroup = TabGroupFixtures.dummy(userId = user.id)
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

                    val numericId = tabGroupRepository.findAllByUserId(user.id).first().id
                    val id = HashIdCodec.encode(numericId)

                    val qrPath =
                        client
                            .post("/tab-group/qr-code") {
                                setBody(
                                    CreateTabGroupQrCodeRequest(
                                        id = id,
                                    ),
                                )
                            }.body<CreateTabGroupQrCodeResponse>()
                            .path

                    // when
                    val response = client.get(qrPath)

                    // then
                    response.status shouldBe HttpStatusCode.OK
                    tabGroupRepository.find(numericId).also {
                        it?.toDto() shouldBe response.body<TabGroupDto>()
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
                                    id = HashIdCodec.encode(id),
                                ),
                            )
                        }

                    // then
                    response.status shouldBe HttpStatusCode.InternalServerError
                }
            }

            describe("만료된 QR 코드로 탭 그룹 조회") {
                val (user, accessToken, _) = bootstrapService.signUp()
                baseTestApplication(accessToken.value) { client ->
                    // given
                    val tabGroup = TabGroupFixtures.dummy(userId = user.id)
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

                    val id = HashIdCodec.encode(tabGroupRepository.findAllByUserId(user.id).first().id)

                    val qrPath =
                        client
                            .post("/tab-group/qr-code") {
                                setBody(
                                    CreateTabGroupQrCodeRequest(
                                        id = id,
                                    ),
                                )
                            }.body<CreateTabGroupQrCodeResponse>()
                            .path

                    clock.tick(Duration.ofSeconds(600))

                    // when
                    val response = client.get(qrPath)

                    // then
                    response.status shouldBe HttpStatusCode.InternalServerError
                }
            }
        }
    })
