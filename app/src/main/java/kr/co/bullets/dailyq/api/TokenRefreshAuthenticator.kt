package kr.co.bullets.dailyq.api

import android.util.Log
import kr.co.bullets.dailyq.AuthManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenRefreshAuthenticator : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // API에 따라 Authorization 헤더를 사용하지 않는 경우가 있기 때문에, 원래의 요청이
        // Authorization 헤더로 액세스 토큰을 보냈는지 확인합니다. 없다면 TokenRefreshAuthenticator에서
        // 처리하지 않기 때문에 null을 반환합니다.
        val accessToken = response.request.header("Authorization")
            ?.split(" ")
            ?.getOrNull(1)

        accessToken ?: return null

        // 리프레시 토큰이 있어야 토큰 갱신을 할 수 있기 때문에 AuthManager에 리프레시 토큰이 있는지 확인하고,
        // 리프레시 토큰이 없다면 null을 반환합니다.
        AuthManager.refreshToken ?: return null

        val api = ApiService.getInstance()

        // 병렬로 요청을 보냈다면 여러 개의 401 응답을 받게 됩니다. 이 경우에 토큰 갱신을 여러번 하게 되고
        // 이미 갱신된 토큰을 다시 갱신하게 되는 문제가 있습니다.
        // 이런 경우가 발생하지 않도록 synchronized 키워드로 토큰 갱신 블럭을 동기화하고 블럭에 진입한 후엔 다시
        // 액세스 토큰이 이미 갱신돼 바뀌지 않았는지 확인한 후 갱신 요청을 보냅니다.
        synchronized(this) {
            if (accessToken == AuthManager.accessToken) {
                val authTokenResponse = api.refreshToken(AuthManager.refreshToken!!).execute().body()!!

                AuthManager.accessToken = authTokenResponse.accessToken
                AuthManager.refreshToken = authTokenResponse.refreshToken
            }
        }

        // 토큰이 갱신되면 새로운 Request를 만들고 Authorization 헤더가 교체된 요청을 만들어 반환합니다.
        return response.request.newBuilder()
            .header("Authorization", "Bearer ${AuthManager.accessToken}")
            .build()
    }
}