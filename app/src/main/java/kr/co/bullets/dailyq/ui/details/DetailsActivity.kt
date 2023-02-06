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
import kr.co.bullets.dailyq.db.entity.QuestionEntity
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
            // [코드 10-43]의 DetailsActivity에서는 ProfileFragment처럼 로컬 데이터베이스에서 먼저 질문을 가져와 표시한 후,
            // API에서 최신의 질문을 가져와 UI와 로컬 데이터베이스를 갱신합니다.
            // 캐시를 사용하면 '답 개수'처럼 변하는 정보는 서버의 최신 정보와 일치하지 않는 경우가 발생할 수 있지만
            // 오프라인에서도 볼 수 있다는 것과 서버 부하를 줄일 수 있다는 장점이 있습니다.
            // 클라이언트의 캐시는 서버 API에 따라 구현이 많이 달라질 수 있으니 서버 개발자와도 상의가 필요하고,
            // 또한 사용자에게 오래된 정보가 보이는 것은 정보의 종류에 따라 버그로 보일 수 있으니
            // 캐시를 사용했을 때 어떤 경우가 있을 수 있는지를 기획자가 알 수 있도록 해야 합니다.
            db.getQuestionDao().get(qid.toString())?.let {
                binding.question.text = it.text
            }

            val questionResponse = api.getQuestion(qid)

            if (questionResponse.isSuccessful) {
                val question = questionResponse.body()
                binding.question.text = question?.text
//                binding.question.text = questionResponse.body()?.text

                question?.let {
                    val questionEntity = QuestionEntity(it.id, it.text, it.answerCount, it.updatedAt, it.createdAt)
                    db.getQuestionDao().insertOrReplace(questionEntity)
                }
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