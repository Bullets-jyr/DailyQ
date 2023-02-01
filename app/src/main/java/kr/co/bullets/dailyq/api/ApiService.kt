package kr.co.bullets.dailyq.api

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import kr.co.bullets.dailyq.AuthManager
import kr.co.bullets.dailyq.api.adapter.LocalDateAdapter
import kr.co.bullets.dailyq.api.converter.LocalDateConverterFactory
import kr.co.bullets.dailyq.api.response.Answer
import kr.co.bullets.dailyq.api.response.AuthToken
import kr.co.bullets.dailyq.api.response.Question
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

        private fun okHttpClient(): OkHttpClient {
            val builder = OkHttpClient.Builder()
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            return builder
                .connectTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                // AuthInterceptor는 애플리케이션 인터셉터로 등록하겠습니다. [코드 6-15]처럼 OkHttp Client.Builder의
                // addInterceptor() 메서드로 추가합니다. 네트워크 인터셉터를 추가하고 싶다면 addNetworkInterceptor() 메서드를 사용합니다.
                .addInterceptor(AuthInterceptor())
                .authenticator(TokenRefreshAuthenticator())
                .addInterceptor(logging)
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
                .baseUrl("http://192.168.0.105:8080")
//                .baseUrl("http://192.168.1.169:8080")
                .client(okHttpClient())
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
    suspend fun editAnswer(@Path("qid") qid: LocalDate, @Field("text") text: String? = null, @Field("photo") photo: String? = null, @Field("uid") uid: String? = "anonymous"): Response<Answer>

    @DELETE("/v2/questions/{qid}/answers/{uid}")
    suspend fun deleteAnswer(@Path("qid") qid: LocalDate, @Path("uid") uid: String? = AuthManager.uid): Response<Unit>
}