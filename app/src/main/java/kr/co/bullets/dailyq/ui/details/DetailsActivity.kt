package kr.co.bullets.dailyq.ui.details

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kr.co.bullets.dailyq.R
import kr.co.bullets.dailyq.databinding.ActivityDetailsBinding
import kr.co.bullets.dailyq.ui.base.BaseActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// [코드 9-9]의 DetailsActivity를 추가합니다. AnswerAdapter에 registerAdapterDataObserver() 메서드로
// 옵저버를 추가해 답의 유무에 따라 '답이 없습니다.'라는 문구를 표시하는 binding.empty의 노출 여부가 결정되도록 만들었습니다.
class DetailsActivity : BaseActivity() {

    companion object {
        const val EXTRA_QID = "qid"
    }

    lateinit var binding: ActivityDetailsBinding
    var adapter: AnswerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val qid = intent?.getSerializableExtra(EXTRA_QID) as LocalDate

        supportActionBar?.title = DateTimeFormatter.ofPattern(getString(R.string.date_format)).format(qid)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = AnswerAdapter(this)
        adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                val itemCount = adapter?.items?.size ?: 0
                binding.empty.isVisible = itemCount == 0
            }
        })

        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        lifecycleScope.launch {
            val questionResponse = api.getQuestion(qid)
            if (questionResponse.isSuccessful) {
                binding.question.text = questionResponse.body()?.text
            }

            val answerResponse = api.getAnswers(qid)
            if (answerResponse.isSuccessful) {
                adapter?.items = answerResponse.body()
            }
        }
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