package kr.co.bullets.dailyq.api

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import kr.co.bullets.dailyq.AuthManager
import kr.co.bullets.dailyq.api.adapter.LocalDateAdapter
import kr.co.bullets.dailyq.api.converter.LocalDateConverterFactory
import kr.co.bullets.dailyq.api.response.*
import okhttp3.Cache
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.time.LocalDate
import java.util.concurrent.TimeUnit

interface ApiService {

    companion object {

        // Retrofit을 싱글톤 패턴으로 만들어 앱 전체에서 하나의 인스턴스를 공유하도록 만들겠습니다.
        // 싱글톤 인스턴스를 보관할 INSTANCE 변수
        private var INSTANCE: ApiService? = null

        // 이제 [코드 9-14]를 보고 캐시를 사용하도록 설정합니다. okHttpClient() 메서드에 Context 매개변수가 추가됐습니다.
        private fun okHttpClient(context: Context): OkHttpClient {
            val builder = OkHttpClient.Builder()
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            // 캐시가 저장될 위치와 사용할 크기를 생성자로 전달해 Cache를 생성하고,
            // OkHttpClient.Builder의 cache() 메서드로 전달함으로써 캐시를 사용하기 위한 설정이 끝납니다.
            val cacheSize = 5 * 1024 * 1024L // 5 MB
            val cache = Cache(context.cacheDir, cacheSize)

            return builder
                .connectTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                // 앱을 시작하고 타임라인에서 질문을 선택해 상세보기 화면으로 돌아오면 로그가 출력됩니다.
                // 다시 타임라인으로 돌아갔다가 같은 질문을 선택해 상세보기에서 로그를 확인합니다.
                // 이제 상세보기로 두 번 진입했습니다.
                // 처음 상세보기로 진입했을 때 ETag로 캐시가 저장되었기 때문에 두 번째 진입 로그는 조금 다른 것을 볼 수 있습니다.
                // 5)는 Daily Q에서 보내는 요청으로 1)과 다르지 않습니다.
                // 6)의 NetworkInterceptor 요청에는 If-None-Match 헤더가 추가된 것을 볼 수 있습니다. 캐시에 저장된 응답에 ETag가 있기 때문에 Okhttp의 CacheInterceptor가 추가를 한 것입니다.
                // 7)에서는 서버가 If-None-Match로 전달된 ETag를 확인하고 응답 메시지에 변경이 없다면 본문 없이 304 Not Modified 응답을 보냅니다. NetworkInterceptor의 응답 로그에서 304를 확인할 수 있습니다.
                // 8) AppInterceptor의 응답 로그는 7)의 NetworkInterceptor의 응답 로그에서 304를 확인할 수 있습니다.
                // 상세보기 기능을 만들고 Retrofit이(정확히는 OkHttp에) 캐시를 사용하도록 설정해 요청과 응답이 어떻게 달라지는지 보았습니다.
                // 캐시를 사용하기 위해 작성한 코드는 [코드 9-14]의 세 줄이 전부입니다.
                // HTTP 캐시는 라이브러리의 지원으로 쉽게 사용할 수 있지만 GET 요청의 응답만을 캐시하고, 캐싱한 리소스를 사용하려면 HTTP 요청을 통해서만 가져올 수 있다는 한계가 있습니다.
                // 10장에서는 로컬 데이터베이스를 캐시로 사용해 HTTP 캐시를 보완하고 일부 기능을 오프라인에서도 사용할 수 있도록 만들겠습니다.
                .cache(cache)
                // AuthInterceptor는 애플리케이션 인터셉터로 등록하겠습니다. [코드 6-15]처럼 OkHttp Client.Builder의
                // addInterceptor() 메서드로 추가합니다. 네트워크 인터셉터를 추가하고 싶다면 addNetworkInterceptor() 메서드를 사용합니다.
                .addInterceptor(AuthInterceptor())
                .authenticator(TokenRefreshAuthenticator())
                .addInterceptor(logging)
                // EndpointLoggingInterceptor는 생성자로 인터셉터의 위치를 구분할 이름과 확인하려는 API URL의 접미사를 전달합니다.
                // OkHttpClient.Builder에 addInterceptor()와 addNetworkInterceptor() 메서드로 등록합니다.
                // 우리가 확인할 로그는 AppInterceptor에서의 요청과 응답, NetworkInterceptor에서의 요청과 응답으로 총 4가지입니다.
                // HttpLoggingInterceptor와 마찬가지로 로그의 시작이 '-->'이면 요청, '<--'이면 응답입니다. 로그의 순서는 다음과 같습니다.
                // 1) AppInterceptor에서 요청
                // 2) NetworkInterceptor에서 요청
                // 3) NetworkInterceptor에서 응답
                // 4) AppInterceptor에서 응답
                // 로그캣 필터에 'DailyQ_answers'를 입력하고 앱을 실행해 상세보기로 이동하면 요청과 응답 로그를 볼 수 있습니다.
                // 다음의 로그에서는 시간과 패키지, 태그 정보는 가독성을 위해 생략했습니다.
                // 주목해서 볼 것은 첫 번째로 3)의 네트워크 인터셉터 응답에 ETag가 있다는 것입니다.
                // 하지만 아직 캐시를 사용하도록 준비하지 않았기 때문에 타임라인으로 나갔다가 다시 상세보기로 돌아와도 요청과 응답이 달라지지 않습니다.
                // 둘째로 3),4)의 Response Code에 세 개의 값이 있다는 것입니다.
                // 첫 번째 코드는 Response 객체에서 바로 가져온 값이고, 두 번째는 네트워크, 세 번째는 캐시에서 가져왔습니다.
                // 이 값들이 어떻게 다른지 캐시를 사용하면서 알아보겠습니다.
                // Response code: ${response.code} (Network: ${response.networkResponse?.code}, Cache: ${response.cacheResponse?.code})
                .addInterceptor(EndpointLoggingInterceptor("AppInterceptor", "answers"))
                .addNetworkInterceptor(EndpointLoggingInterceptor("NetworkInterceptor", "answers"))
                .build()
        }

        // Retrofit을 만들던 create() 메서드는 private으로 변경해 외부에서 호출할 수 없도록 했습니다.
        private fun create(context: Context): ApiService {
            // 응답은 Gson이 변환하기 때문에 별도의 작업이 필요합니다.
            // 4장의 커스텀 직렬화와 역직렬화에서 본 것처럼 Gson이 LocalDate를 처리할 수 있도록 LocalDateAdapter를 만들고
            // Gson.Builder에 등록합니다.
            val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter)
                .create()

            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                // Retrofit으로 요청을 보낼 때 LocalDate를 변환할 수 있도록 LocalDateConverterFactory를 만들어서 등록했지만,
                .addConverterFactory(LocalDateConverterFactory())
                .baseUrl("http://192.168.0.106:8080")
//                .baseUrl("http://192.168.1.26:8080")
                .client(okHttpClient(context))
                .build()
                .create(ApiService::class.java)
        }

        // INSTANCE 변수를 초기화하는 init() 메서드
        // init() 메서드에서 create() 메서드로 인스턴스를 생성해 INSTANCE에 할당합니다.
        // init() 메서드는 Context를 매개변수로 받고 앱의 ApiService를 사용하기 전에 호출이 보장되어야 합니다.
        // 이 조건에 가장 알맞은 곳 Application의 onCreate()입니다.
        // Application을 상속받은 App 클래스를 만들고 ApiService를 초기화합니다.
        fun init(context: Context) = INSTANCE ?: synchronized(this) {
            INSTANCE ?: create(context).also {
                INSTANCE = it
            }
        }

        // INSTANCE에 접근할 수 있는 getInstance() 메서드
        // getInstance() 메서드를 호출하는 곳에서는 이미 INSTANCE가 생성되었다고 가정합니다.
        // 따라서 init() 메서드가 먼저 실행되었어야 합니다.
        fun getInstance(): ApiService = INSTANCE!!
    }

    /**
     * Retrofit의 여러 가지 기능을 사용했습니다. 하나씩 살펴보겠습니다.
     *
     * 메서드의 반환값을 제네릭 클래스인 Response<T>로 선언하고 DTO(Data Transfer Object) 클래스를
     * 타입 매개변수로 사용했습니다. [코드 5-4] ApiService.getQuestion() 메서드처럼 Question을
     * 직접 반환값으로 받으면 HTTP 응답의 코드나 헤더와 같은 정보를 사용할 수 없다는 문제가 있습니다.
     * Response는 code() 메서드로 HTTP 응답 코드를 가져올 수 있고, headers() 메서드로는 HTTP 응답 헤더를
     * 가져올 수 있습니다. 그리고 body() 메서드로는 타입 매개변수로 선언했던 응답의 본문을 가져올 수 있습니다.
     *
     * 다음으로 @POST와 @PUT 어노테이션을 알아보겠습니다. HTTP의 POST와 PUT 요청은 본문을 갖는데, Retrofit에서
     * 본문을 구성하는 방법은 세 가지가 있습니다.
     *
     * 1) @FormUrlEncoded
     * 이 어노테이션은 요청의 Content-Type을 application/x-www-form-urlencoded로 만듭니다.
     * 그리고 메서드의 매개변수 중에서 @Field 어노테이션이 붙은 것을 본문으로 만듭니다.
     *
     * 2) @Multipart
     * 파일을 보내거나 여러 타입의 데이터를 하나의 요청으로 보내기 위해서는 multipart/form-data로 요청을 보내야 합니다.
     * Retrofit에서는 메서드에 @Multipart를 붙여 만들 수 있습니다. MultipartBody.Part 타입의 매개변수에 @Part를 붙여
     * 각 파트를 정의합니다.
     *
     * 3) Json
     * 마지막은 이 책에서는 사용하지 않지만 JSON으로 요청을 보낼 때 주로 사용하는 방법입니다.
     * 요청에 전달할 매개변수를 객체로 만들고 메서드의 매개변수로 전달합니다.
     * 이 매개변수에 @Body 어노테이션을 붙이면 Retrofit에 등록된 컨버터가 객체를 직렬화하여 요청의 본문으로 사용합니다.
     * @POST("/v1/questions/{qid}/answers")
     * suspend fun writeAnswer(@Path("qid") qid: LocalDate, @Body params: WriteParams): Response<Answer>
     * WriteParams 객체를 만들어 writeAnswer() 메서드의 매개변수로 사용했습니다. 이렇게 하면 HTTP 메시지에서
     * Content-Type이 application/json이 되고, WriteParams가 본문의 JSON이 됩니다.
     */

    /**
     * 응답으로 받은 AuthToken은 로그아웃을 하기 전까지 계속 사용을 해야 하기 때문에 앱이 종료된 후에도 보관할 수 있도록 SharedPreferences에 보관하겠습니다.
     * SharedPreferences는 값을 불러오거나 저장할 때 매번 키와 타입을 지정해야 해서 실수하기 쉽습니다. 그래서 SharedPreferences를 멤버변수로 갖는 AuthManager를 만들고
     * AuthManager의 멤버변수에 getter와 setter를 정의해 사용자가 직접 SharedPreferences를 호출하다 실수하는 것을 방지하겠습니다.
     * 또한 object 키워드로 싱글톤을 만들어 앱의 어느 곳에서나 바로 사용할 수 있도록 합니다. 주의할 점은 최초에 SharedPreferences를 불러올 때 Context가 필요하기 때문에
     * 사용하기 전에 최초 1회 init() 메서드를 호출해 context를 전달하여 초기화를 해야 합니다.
     */

    @FormUrlEncoded
    @POST("/v2/token")
    suspend fun login(@Field("username") uid: String, @Field("password") password: String, @Field("grant_type") grantType: String = "password", @Tag authType: AuthType = AuthType.NO_AUTH): Response<AuthToken>

    @FormUrlEncoded
    @POST("/v2/token")
    fun refreshToken(@Field("refresh_token") refreshToken: String, @Field("grant_type") grantType: String = "refresh_token", @Tag authType: AuthType = AuthType.NO_AUTH): Call<AuthToken>

    // FCM 백엔드에서 발급받은 토큰을 API 서버 전달하기 위한 '푸시 토큰 등록' API를 추가합니다.
    @FormUrlEncoded
    @POST("/v2/user/push-tokens")
    suspend fun registerPushToken(@Field("token") pushToken: String): Response<Unit>

    @GET("/v2/questions")
    suspend fun getQuestions(@Query("from_date") fromDate: LocalDate, @Query("page_size") pageSize: Int): Response<List<Question>>

    @GET("v2/questions/{qid}")
//    suspend fun getQuestion(@Path("qid") qid: String): Question
//    suspend fun getQuestion(@Path("qid") qid: LocalDate): Question
    suspend fun getQuestion(@Path("qid") qid: LocalDate): Response<Question>

    @GET("/v2/questions/{qid}/answers/{uid}")
    suspend fun getAnswer(@Path("qid") qid: LocalDate, @Path("uid") uid: String? = AuthManager.uid): Response<Answer>

    @FormUrlEncoded
    @POST("/v2/questions/{qid}/answers")
    suspend fun writeAnswer(@Path("qid") qid: LocalDate, @Field("text") text: String? = null, @Field("photo") photo: String? = null): Response<Answer>

    @FormUrlEncoded
    @PUT("/v2/questions/{qid}/answers/{uid}")
    suspend fun editAnswer(@Path("qid") qid: LocalDate, @Field("text") text: String? = null, @Field("photo") photo: String? = null, @Field("uid") uid: String? = AuthManager.uid): Response<Answer>

    // 상세보기에서는 질문의 모든 답을 가져와 표시합니다. '답 목록 가져오기' API를 추가합니다.
    @GET("/v2/questions/{qid}/answers")
    suspend fun getAnswers(@Path("qid") qid: LocalDate): Response<List<Answer>>

    @DELETE("/v2/questions/{qid}/answers/{uid}")
    suspend fun deleteAnswer(@Path("qid") qid: LocalDate, @Path("uid") uid: String? = AuthManager.uid): Response<Unit>

    // Image를 응답으로 갖는 uploadImage() 메서드([코드 7-3])를 선언합니다.
    @Multipart
    @POST("/v2/images")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Response<Image>

    @GET("/v2/users/{uid}")
    suspend fun getUser(@Path("uid") uid: String): Response<User>

    // '팔로우'와 '팔로우 취소' API는 응답에 본문 없이 없기 때문에 Response의 타입 매개변수를 Unit으로 합니다.
    @POST("/v2/user/following/{uid}")
    suspend fun follow(@Path("uid") uid: String): Response<Unit>

    @DELETE("/v2/user/following/{uid}")
    suspend fun unfollow(@Path("uid") uid: String): Response<Unit>

    @GET("/v2/users/{uid}/answers")
    suspend fun getUserAnswers(@Path("uid") uid: String, @Query("from_date") fromDate: LocalDate? = null): Response<List<QuestionAndAnswer>>
}