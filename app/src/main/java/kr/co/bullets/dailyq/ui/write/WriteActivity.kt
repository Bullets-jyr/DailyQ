package kr.co.bullets.dailyq.ui.write

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kr.co.bullets.dailyq.R
import kr.co.bullets.dailyq.api.asRequestBody
import kr.co.bullets.dailyq.api.response.Answer
import kr.co.bullets.dailyq.api.response.Question
import kr.co.bullets.dailyq.databinding.ActivityWriteBinding
import kr.co.bullets.dailyq.ui.base.BaseActivity
import okhttp3.MultipartBody
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
    var imageUrl: String? = null

    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                val imageUri = result.data?.data ?: return@launch
                // 앞에서 만든 asRequestBody() 메서드로 RequestBody를 만들고
                val requestBody = imageUri.asRequestBody(contentResolver)

                // 다시 createFormData() 메서드로 MultipartBody.Part를 만들었습니다.
                // 파일 이름은 다음의 코드처럼 콘텐트 리졸버에서 가져올 수 있지만 업로드했을 때 사용하지 않기 때문에
                // 쿼리를 하지 않고 임의의 문자열인 'filename'을 사용했습니다.

                // val cursor = contentResolver.query(this, null, null, null, null)
                // val filename = cursor.getString(cursor.getColoumIndex(OpenableColums.DISPLAY_NAME))

                val part = MultipartBody.Part.createFormData("image", "filename", requestBody)

                // 응답으로 받은 이미지 URL로 화면 하단의 섬네일을 표시합니다.
                val imageResponse = api.uploadImage(part)

                if (imageResponse.isSuccessful) {
                    // 이미지를 업로드하고 응답으로 받은 URL은 액티비티의 멤버변수 imageUrl에 보관하다가 완료 버튼을 터치해
                    // writeAnswer나 editAnser가 호출될 때 함게 보내야 합니다.
                    imageUrl = imageResponse.body()!!.url

                    // 섬네일 이미지의 모서리를 둥글게 만들기 위해 Coil로 이미지를 불러올 때 RoundedCornersTransformation을 사용했습니다.
                    // RoundedCornersTransformation은 하나의 반지름(radius)을 전달해 모든 모서리를 같은 모양으로 만들 수 있고,
                    // 각 모서리의 반지름을 전달해 여러 가지 모양으로도 만들 수 있습니다.
                    binding.photo.load(imageUrl) {
                        transformations(RoundedCornersTransformation(resources.getDimension(R.dimen.thumbnail_rounded_corner)))
                    }

                    binding.photoArea.isVisible = true
                }
            }
        }
    }

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

            imageUrl = answer?.photo
            binding.photoArea.isVisible = !imageUrl.isNullOrEmpty()

            imageUrl?.let {
                binding.photo.load(it) {
                    transformations(RoundedCornersTransformation(resources.getDimension(R.dimen.thumbnail_rounded_corner)))
                }
            }

            binding.photoArea.setOnClickListener {
                showDeleteConfirmDialog()
            }
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
            // WriteActivity에서는 먼저 앱바의 사진 추가 메뉴를 터치했을 때 갤러리를 열고 사진을 가져와 서버로 사진을 올리도록 만들겠습니다.
            R.id.add_photo -> {
                // Intent의 타입과 Intent.EXTRA_MIME_TYPES를 이용해 처리할 수 있는 이미지 타입을 전달합니다.
                // 이 Intent는 카메라가 아니라 사진을 선택하는 앱을 시작하기 때문에 미리 사진이 준비되어 있어야 합니다.
                // 기기의 카메라 앱으로 사진을 준비해 둡니다.
                // 이미지를 가져오면 startForResult의 ActivityResultCallback으로 URI가 전달됩니다.
                startForResult.launch(Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                })
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

                // writeAnswer와 editAnswer의 마지막에 imageUrl을 전달합니다.
                api.writeAnswer(question.id, text, imageUrl)
            } else {
                Log.d("write", "api.editAnswer, ${question.id}")

                // writeAnswer와 editAnswer의 마지막에 imageUrl을 전달합니다.
                api.editAnswer(question.id, text, imageUrl)
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

    // 이미 업로드한 이미지를 삭제하려면 writeAnswer나 editAnswer를 호출할 때 이미지 주소를 보내지 않으면 됩니다.
    // 이를 위해서 섬네일을 터치했을 때 삭제 다이얼로그를 표시하고 imageUrl을 null로 만들고 사진 영역을 숨깁니다.
    fun showDeleteConfirmDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.dialog_msg_are_you_sure_to_delete)
            .setPositiveButton(android.R.string.ok) { dialog, which ->
                binding.photo.setImageResource(0)
                binding.photoArea.isVisible = false
                imageUrl = null
            }.setNegativeButton(android.R.string.cancel) { dialog, which ->

            }.show()
    }
}