package kr.co.bullets.dailyq

import android.app.Application
import kr.co.bullets.dailyq.api.ApiService
import kr.co.bullets.dailyq.db.AppDatabase

class App : Application() {

    // 앱을 실행하면 App.onCreate()에서 ApiService.init() 메서드가 호출됩니다.
    override fun onCreate() {
        super.onCreate()

        AuthManager.init(this)
        ApiService.init(this)
        AppDatabase.init(this)
    }
}