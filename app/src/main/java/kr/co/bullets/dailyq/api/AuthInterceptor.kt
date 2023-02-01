package kr.co.bullets.dailyq.api

import android.util.Log
import kr.co.bullets.dailyq.AuthManager
import okhttp3.Interceptor
import okhttp3.Response

// OkHttp는 [그림 6-8]과 같이 앱과 OkHttp 사이에 위치하는 애플리케이션 인터셉터와 OkHttp와 네트워크 사이에 위치하는
// 네트워크 인터셉터를 지원합니다. 인터셉터는 모든 요청과 응답의 사이에서 이들을 모니터링하거나 변경, 또는 재시도할 수 있습니다.
// OkHttp도 인터셉터를 이용해 리다이렉트 응답을 처리하거나 로컬 캐시에서 값을 가져옵니다.
// 우리는 인터셉터를 이용해서 AuthManager에서 보관하는 액세스 토큰을 요청의 헤더에 추가하겠습니다.
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()

        // [코드 6-20]의 AuthInterceptor에서는 request 객체의 tag() 메서드로 가져오려는 타입을 전달해 인자로 전달된 값을 가져오는 것을 볼 수 있습니다.
        // Daily Q API는 대부분 액세스 토큰이 필요하기 때문에 AuthType.ACCESS_TOKEN을 기본값으로 정하고
        // authTypedl AuthType.ACCESS_TOKEN인 경우에만 Authorization 헤더를 추가하겠습니다.
        val authType = request.tag(AuthType::class.java) ?: AuthType.ACCESS_TOKEN

        when (authType) {
            AuthType.NO_AUTH -> {
                Log.d("okhttp", "AuthType.NO_AUTH")
            }
            AuthType.ACCESS_TOKEN -> {
                Log.d("okhttp", "AuthType.ACCESS_TOKEN")
                AuthManager.accessToken?.let { token ->
                    builder.header("Authorization", "Bearer $token")
                }
            }
        }

        return chain.proceed(builder.build())
    }
}