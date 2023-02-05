package kr.co.bullets.dailyq.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

// Okhttp에는 [그림 6-8]에서 볼 수 있는 것 처럼 두 종류의 인터셉터가 있습니다.
// 애플리케이션 인터셉터는 Daily Q와 OkHttp 사이에 위치하고 네트워크 인터셉터는 Okhttp와 네트워크 사이에 위치합니다.
// 캐시를 사용하면 OkHttp가 네트워크와 Daily Q 사이에서 요청과 응답을 변경합니다.
// 앞에서 등록한 HttpLoggingInterceptor는 모든 요청과 응답을 출력하므로 캐시 관련 로그만 보기엔 다소 불편하고,
// 두 곳에 등록했을 때 구분하기도 어려워, 특정 API의 로그만 출력하는 EndPointLoggingEnterceptor를 만들겠습니다.
// EndpointLoggingInterceptor는 생성자로 인터셉터의 위치를 구분할 이름과 확인하려는 API URL의 접미사를 전달합니다.
// OkHttpClient.Builder에 addInterceptor()와 addNetworkInterceptor() 메서드로 등록합니다.
class EndpointLoggingInterceptor(val name: String, val urlSuffix: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.method != "GET" || !request.url.encodedPath.endsWith(urlSuffix)) {
            return chain.proceed((request))
        }

        Log.i("DailyQ_$urlSuffix",
            """--> $name
                |${request.url}
                |${request.headers}""".trimMargin())

        val response = chain.proceed(request)

        Log.i("DailyQ_$urlSuffix",
            """<-- $name
                |Response code: ${response.code} (Network: ${response.networkResponse?.code}, Cache: ${response.cacheResponse?.code})
                |${response.headers}""".trimMargin())

        return response
    }
}