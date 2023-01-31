package kr.co.bullets.dailyq.ui.write

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.bullets.dailyq.R
import kr.co.bullets.dailyq.api.response.Answer
import kr.co.bullets.dailyq.api.response.Question
import kr.co.bullets.dailyq.databinding.ActivityWriteBinding
import kr.co.bullets.dailyq.ui.base.BaseActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WriteActivity : BaseActivity() {

    companion object {
        const val EXTRA_QID = "qid"
        const val EXTRA_MODE = "mode"
    }

    enum class Mode {
        WRITE, EDIT
    }

    lateinit var binding: ActivityWriteBinding
    lateinit var mode: Mode

    lateinit var question: Question
    var answer: Answer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // WriteActivity에서는 새 답을 작성하거나 수정할 수 있습니다. 어떤 질문인지는 WriteActivity를 시작하는 곳에서 intent를 통해서 전달합니다.
        val qid = intent.getSerializableExtra(EXTRA_QID) as LocalDate
        mode = intent?.getSerializableExtra(EXTRA_MODE)!! as Mode

        // intent로 전달받은 qid를 파싱해 타이틀에 날짜를 표시하고
        supportActionBar?.title = DateTimeFormatter.ofPattern(getString(R.string.date_format)).format(qid)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // '질문 가져오기' API와 '답 가져오기' API로 질문과 내가 쓴 답을 가져와 UI에 표시합니다.
        lifecycleScope.launch {
            question = api.getQuestion(qid).body()!!
            answer = api.getAnswer(qid).body()

            binding.question.text = question.text
            binding.answer.setText(answer?.text)
        }
    }

    // 앱바에 이미지 첨부 버튼과 완료 버튼을 추가하기 위해 onCreateOptionsMenu에서 [코드 5-16]에서 만든 R.menu.write_menu를 인플레이트합니다.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.write_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // 그리고 onOptionsItemSelected에서 '완료' 버튼을 눌렀을 때 write() 메서드를 호출했습니다.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // WriteActivity에서 완료 버튼을 터치하면 '글쓰기 API'를 호출합니다.
            R.id.done -> {
                write()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // write() 메서드에서는 mode 변수의 값에 따라 writeAnswer()나 editAnswer()로 분기합니다.
    fun write() {
        val text = binding.answer.text.toString().trimEnd()
        Log.d("write", "$text")
        lifecycleScope.launch {
            val answerResponse = if (answer == null) {
                Log.d("write", "api.writeAnswer, ${question.id}")
                api.writeAnswer(question.id, text)
            } else {
                Log.d("write", "api.editAnswer, ${question.id}")
                api.editAnswer(question.id, text)
            }
            Log.d("write", "answerResponse.isSuccessful, ${answerResponse.isSuccessful}")
            // 성공 응답을 받으면 MainActivity로 결과를 전달하기 위해 setResult() 메서드를 호출하고 finish() 메서드로 종료합니다.
            if (answerResponse.isSuccessful) {
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this@WriteActivity, answerResponse.message(), Toast.LENGTH_SHORT).show()
            }
        }
    }
}