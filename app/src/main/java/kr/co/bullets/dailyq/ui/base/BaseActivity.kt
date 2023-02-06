package kr.co.bullets.dailyq.ui.base

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import kr.co.bullets.dailyq.R
import kr.co.bullets.dailyq.api.ApiService
import kr.co.bullets.dailyq.db.AppDatabase

// UserDao를 사용하려면 AppDatabase.getInstance()를 호출한 후 다시 getUserDao()를 호출해야합니다.
// 매번 이렇게 사용하기엔 코드가 길어 가독성이 떨어지기 때문에 BaseFragment.kt와 BaseActivity.kt에 짧은 멤버변수로 접근할 수 있게 추가하겠습니다.
// Room을 사용할 준비가 끝났습니다.
// 다시 ProfileFragment로 돌아가 API에서 받은 사용자 정보를 데이터베이스에 저장하고, 데이터베이스에서 사용자 정보를 불러와 표시하도록 만들겠습니다.
abstract class BaseActivity : AppCompatActivity() {
    val api: ApiService by lazy {
        ApiService.getInstance()
    }

    val db: AppDatabase by lazy {
        AppDatabase.getInstance()
    }

    // 앱바의 뒤로가기 버튼을 터치했을 때 액티비티를 종료하도록 만듭니다.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}