package kr.co.bullets.dailyq.ui.profile

import android.os.Bundle
import android.view.MenuItem
import kr.co.bullets.dailyq.R
import kr.co.bullets.dailyq.databinding.ActivityProfileBinding
import kr.co.bullets.dailyq.ui.base.BaseActivity

// 앱을 실행하면 ProfileFragment를 확인할 수 있습니다. 그런데 MainActivity에서는
// 내 프로필만 볼 수 있기 때문에 팔로우 버튼의 작동을 확인할 수 없는 문제가 있습니다.
// [스토리보드 10-2], [디자인 가이드 10-2]를 보고 ProfileActivity를 만들어 다른 사람의 프로필을 볼 수 있도록 만들고,
// 상세보기에서 답을 쓴 사용자의 사진을 터치했을 때 ProfileActivity를 시작하도록 만들겠습니다.
class ProfileActivity : BaseActivity() {

    companion object {
        const val EXTRA_UID = "uid"
    }

    lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = intent.getStringExtra(EXTRA_UID)

        supportActionBar?.title = uid
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val fragment = ProfileFragment()
            fragment.arguments = Bundle().apply {
                putString(ProfileFragment.ARG_UID, uid)
            }
            val ft = supportFragmentManager.beginTransaction()
            ft.add(R.id.host, fragment).commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}