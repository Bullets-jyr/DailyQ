package kr.co.bullets.dailyq.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.bullets.dailyq.R
import kr.co.bullets.dailyq.databinding.ActivityMainBinding
import kr.co.bullets.dailyq.ui.profile.ProfileFragment
import kr.co.bullets.dailyq.ui.timeline.TimelineFragment
import kr.co.bullets.dailyq.ui.today.TodayFragment

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navView.setOnItemSelectedListener {
            val ft = supportFragmentManager.beginTransaction()

            when (it.itemId) {
                R.id.timeline -> {
                    ft.replace(R.id.host, TimelineFragment())
                    supportActionBar?.setTitle(R.string.title_timeline)
                }
                R.id.today -> {
                    ft.replace(R.id.host, TodayFragment())
                    supportActionBar?.setTitle(R.string.title_today)
                }
                R.id.profile -> {
                    ft.replace(R.id.host, ProfileFragment())
                    supportActionBar?.setTitle(R.string.title_profile)
                }
            }
            ft.commit()
            true
        }

        // MainActivity가 시작할 때 오늘의 질문이 선택되도록 했습니다.
        binding.navView.selectedItemId = R.id.today
    }
}