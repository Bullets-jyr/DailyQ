package kr.co.bullets.dailyq.ui.image

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import coil.load
import kr.co.bullets.dailyq.R
import kr.co.bullets.dailyq.databinding.ActivityImageViewerBinding
import kr.co.bullets.dailyq.ui.base.BaseActivity

class ImageViewerActivity : BaseActivity() {

    companion object {
        const val EXTRA_URL = "url"
    }

    lateinit var binding: ActivityImageViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // [코드 7-10]의 ImageViewerActivity는 앱바의 닫기 버튼 아이콘을 교체하고 Intent로 받은 URL을 사용해 이미지를 로드합니다.
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val url = intent.getStringExtra(EXTRA_URL)
        binding.image.load(url)
    }

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