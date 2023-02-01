package kr.co.bullets.dailyq.api.response

/**
 * API v2에서는 요청을 보낼 때 인증 정보를 함께 전송해야 합니다.
 * '토큰 발급/갱신' API에서 인증에 사용할 토큰을 받아, 모든 요청의 Authorization 헤더를 통해 전달하겠습니다.
 * API 문서의 '토큰 발급/갱신' API를 보고 응답을 받을 때 사용할 AuthToken를 만듭니다.
 */
data class AuthToken(
    val accessToken: String,
    val refreshToken: String
)
