package kr.co.bullets.dailyq.ui.base

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import kr.co.bullets.dailyq.R

abstract class BaseActivity : AppCompatActivity() {

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