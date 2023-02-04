package kr.co.bullets.dailyq.api.response

import java.util.Date

// 상세보기 화면에서는 모든 답에 답을 쓴 사용자가 표시되는데
// '답 목록 가져오기' API의 응답에 사용되는 Answer 클래스에는 답을 쓴 사람의 정보가 빠져있습니다.
// API 문서를 보면 API v2에서는 '답 가져오기' API에서도 답을 쓴 사람의 정보를 내려주는 것을 볼 수 있습니다.
// answerer 필드는 '사용자 정보 가져오기' API의 응답과 같은 구조로, API에서 사용자 정보를 받아올 때 공통으로 사용합니다.
// 사용자 정보를 [코드 9-2]의 User 클래스로 만들어 사용합니다.
data class User(
    val id: String,
    val name: String,
    val description: String?,
    val photo: String?,
    val answerCount: Int,
    val follwerCount: Int,
    val updatedAt: Date
)
