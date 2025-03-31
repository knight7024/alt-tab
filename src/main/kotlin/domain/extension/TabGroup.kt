package com.example.domain.extension

import com.example.domain.user.UserId
import java.time.Instant

/**
 * @param id 탭 그룹 id
 * @param userId 탭 그룹을 생성한 유저 id
 * @param secret E2EE에 사용될 암호화된 시크릿 키
 * @param salt E2EE에 사용될 salt
 * @param tabs 탭 정보들
 */
data class TabGroup(
    val id: Long,
    val userId: UserId,
    val secret: String,
    val salt: String,
    val tabs: List<BrowserTabInfo>,
)

/**
 * @param windowId 브라우저의 윈도우 id. 탭이 어느 윈도우에 존재하는지 구분하기 위해 사용한다.
 * @param groupId 그룹 id. 브라우저마다 명칭이 다르거나 존재하지 않을 수도 있다.
 * @param tabIndex 탭 인덱스
 * @param title 탭 제목
 * @param url 탭 주소
 * @param faviconUrl 탭 파비콘 주소
 * @param incognito 시크릿 모드 여부
 * @param scrollPosition 스크롤의 마지막 위치
 * @param lastUsedAgent 마지막으로 해당 탭을 저장한 에이전트
 * @param lastActiveAt 탭을 마지막으로 실행한 시간
 * @param session 암호화된 웹 세션
 * @param cookie 암호화된 웹 쿠키
 */
data class BrowserTabInfo(
    val windowId: String,
    val groupId: String? = null,
    val tabIndex: Int,
    val title: String,
    val url: String,
    val faviconUrl: String?,
    val incognito: Boolean,
    val scrollPosition: RelativeRatio,
    val lastUsedAgent: String,
    val lastActiveAt: Instant,
    val session: String,
    val cookie: String,
)

data class RelativeRatio(
    val x: Double,
    val y: Double,
)
