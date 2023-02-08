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
        // 안드로이드 8(API 26)부터는 미리 알림 채널을 만들어야 하기 때문에 안드로이드 앱이 시작할 때 채널을 만듭니다.
        // 이를 위해 답글과 팔로우를 위한 채널을 만드는 메서드를 각각 만들고, init() 메서드에서 호출하고 App.kt에서 호출해 채널을 만듭니다.
        Notifier.init(this)
    }
}