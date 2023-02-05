package kr.co.bullets.dailyq.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.bullets.dailyq.AuthManager
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

        binding.navView.setOnNavigationItemSelectedListener {
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
                // [코드 10-9]에서는 MainActivity의 setOnNavigationItemSelectedListener에서
                // ProfileFragment를 만들 때 표시할 사용자의 아이디를 전달하도록 수정합니다.
                // 메인 탭에서는 항상 로그인한 사용자가 표시됩니다.
                // AuthManager에 보관하는 사용자 아이디를 전다합니다.
                R.id.profile -> {
//                    ft.replace(R.id.host, ProfileFragment())
                    ft.replace(R.id.host, ProfileFragment().apply {
                        arguments = Bundle().apply {
                            putString(ProfileFragment.ARG_UID, AuthManager.uid)
                        }
                    })
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