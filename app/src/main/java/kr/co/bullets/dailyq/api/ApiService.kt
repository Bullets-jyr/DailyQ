package kr.co.bullets.dailyq.api

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import kr.co.bullets.dailyq.api.converter.LocalDateConverterFactory
import kr.co.bullets.dailyq.api.response.Question
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.time.LocalDate

interface ApiService {

    companion object {

        // Retrofit을 싱글톤 패턴으로 만들어 앱 전체에서 하나의 인스턴스를 공유하도록 만들겠습니다.
        // 싱글톤 인스턴스를 보관할 INSTANCE 변수
        private var INSTANCE: ApiService? = null

        // Retrofit을 만들던 create() 메서드는 private으로 변경해 외부에서 호출할 수 없도록 했습니다.
        private fun create(context: Context): ApiService {
            val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(LocalDateConverterFactory())
                .baseUrl("http://192.168.0.105:8080")
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

    @GET("v1/questions/{qid}")
//    suspend fun getQuestion(@Path("qid") qid: String): Question
    suspend fun getQuestion(@Path("qid") qid: LocalDate): Question
}