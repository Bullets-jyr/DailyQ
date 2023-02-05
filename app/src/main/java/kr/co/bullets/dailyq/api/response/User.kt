package kr.co.bullets.dailyq.api.response

import java.util.Date

// 상세보기 화면에서는 모든 답에 답을 쓴 사용자가 표시되는데
// '답 목록 가져오기' API의 응답에 사용되는 Answer 클래스에는 답을 쓴 사람의 정보가 빠져있습니다.
// API 문서를 보면 API v2에서는 '답 가져오기' API에서도 답을 쓴 사람의 정보를 내려주는 것을 볼 수 있습니다.
// answerer 필드는 '사용자 정보 가져오기' API의 응답과 같은 구조로, API에서 사용자 정보를 받아올 때 공통으로 사용합니다.
// 사용자 정보를 [코드 9-2]의 User 클래스로 만들어 사용합니다.
// '사용자 정보 가져오기' API의 응답으로 사용하는 User 클래스는 9장에서 '답 목록 가져오기' API를 만들면서 추가했습니다.
// 그런데 '사용자 정보 가져오기' API에는 '답 목록 가져오기' API의 User에서는 볼 수 없던 is_following 필드가 있습니다.
// is_following 필드는 API를 호출하는 사용자에 따라 다른 값을 갖기 때문에 서버에서 추가 연산이 필요합니다.
// 이런 속성은 서버에서 불필요한 연산을 줄이기 위해 API에 따라 생각하기도 합니다.
// 여기서는 User 모델에서 null을 허용하는 isFollowing 속성을 추가합니다.

data class User(
    val id: String,
    val name: String,
    val description: String?,
    val photo: String?,
    val answerCount: Int,
    val followerCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean?,
    val updatedAt: Date
)
