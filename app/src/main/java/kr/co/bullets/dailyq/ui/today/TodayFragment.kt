package kr.co.bullets.dailyq.ui.today

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kr.co.bullets.dailyq.R
import kr.co.bullets.dailyq.api.response.Question
import kr.co.bullets.dailyq.databinding.FragmentTodayBinding
import kr.co.bullets.dailyq.ui.base.BaseFragment
import kr.co.bullets.dailyq.ui.image.ImageViewerActivity
import kr.co.bullets.dailyq.ui.write.WriteActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TodayFragment : BaseFragment() {

    var _binding: FragmentTodayBinding? = null
    val binding
        get() = _binding!!

    var question: Question? = null

    // AndroidX의 Activity와 Fragment에서는 startActivityForResult() 사용을 권장하지 않습니다.
    // registerForActivityResult로 콜백을 등록하면 ActivityResultLauncher를 반환합니다.
    // ActivityResultLauncher의 launch() 메서드로 Intent를 전달해 WriteActivity를 시작합니다.
    // startActivity()를 startForResult.launch()로 교체합니다.
    // WriteActivity의 결과가 TodayFragment의 ActivityResultCallback으로 전달됩니다.
    // 답을 가져와 레이아웃에 구성하는 코드가 onViewCreated와 ActivityResultCallback에서 중복으로 사용되기 때문에
    // setupAnswer() 메서드로 만들어 사용합니다.
    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                setupAnswer()
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    // [코드 5-29]까지 작성한 수 앱을 실행해 오늘의 질문에 답을 쓰고 완료 버튼을 터치합니다.
    // WriteActivity가 종료되고 TodayFragment로 돌아오지만 작성한 답을 볼 수는 없습니다.
    // [코드 5-29]에서 TodayFragment의 onViewCreated()에서 답을 가져와 표시하게 만들었고
    // WriteActivity에서 답을 쓰고 돌아오는 과정에서는 onViewCreated()가 다시 호출되지 않기 때문에
    // 처음 TodayFragment가 생성된 시점의 답이 표시됩니다. 다른 탭으로 이동했다가 돌아오면 정상적으로 답이 표시되는 것을 볼 수 있습니다.
    // 이런 문제는 개발할 때 자주 만나는데 가장 쉬운 해결 방법은 API를 호출해 UI를 갱신하는 코드를 onResume()으로 옮기는 것입니다.
    // 이렇게 하면 WriteActivity에서 답을 쓰고 돌아올 때 onResume()이 호출돼 정상적으로 표시됩니다.
    // 하지만 onResume()은 답이 변경되지 않았을 때에도 호출이 되기 때문에 복잡한 데이터나 사용자가 많은 서비스의 경우엔
    // 서버 부하에 나쁜 영향을 줄 수 있습니다. 다른 방법은 답을 쓰거나 변경했을 때 TodayFragment에 알려줘 UI를 갱신하는 것입니다.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // writeButton과 editButton을 터치했을 때 WriteActivity를 시작하도록 만듭니다.
        // WriteActivity을 시작할 때 질문 아이디(qid)와 모드를 전달해야 합니다.
        binding.writeButton.setOnClickListener {
//            startActivity(Intent(requireContext(), WriteActivity::class.java).apply {
//                putExtra(WriteActivity.EXTRA_QID, question!!.id)
//                putExtra(WriteActivity.EXTRA_MODE, WriteActivity.Mode.WRITE)
//            })
            startForResult.launch(Intent(requireContext(), WriteActivity::class.java).apply {
                putExtra(WriteActivity.EXTRA_QID, question!!.id)
                putExtra(WriteActivity.EXTRA_MODE, WriteActivity.Mode.WRITE)
            })
        }
        binding.editButton.setOnClickListener {
//            startActivity(Intent(requireContext(), WriteActivity::class.java).apply {
//                putExtra(WriteActivity.EXTRA_QID, question!!.id)
//                putExtra(WriteActivity.EXTRA_MODE, WriteActivity.Mode.EDIT)
//            })
            startForResult.launch(Intent(requireContext(), WriteActivity::class.java).apply {
                putExtra(WriteActivity.EXTRA_QID, question!!.id)
                putExtra(WriteActivity.EXTRA_MODE, WriteActivity.Mode.EDIT)
            })
        }
        // 삭제 버튼을 클릭하면 다이얼로그를 표시하고 다시 한번 확인합니다.
        binding.deleteButton.setOnClickListener {
            showDeleteConfirmDialog()
        }
/*
        Thread {
            val url = URL("http://192.168.0.105:8080/v1/hello-world")

            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")
            conn.connect()

            val reader = BufferedReader(InputStreamReader(conn.inputStream))
            val body = reader.readText()
            Log.d("onViewCreated", "$body")
            reader.close()
            conn.disconnect()

//            val json = JSONObject(body)
//            val date = json.getString("date")
//            val message = json.getString("message")

            val gson = Gson()
            val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.KOREA)
            val helloWorld = gson.fromJson(body, HelloWorld::class.java)

            activity?.runOnUiThread {
//                binding.date.text = date
//                binding.question.text = message
                binding.date.text = dateFormat.format(helloWorld.date)
                binding.question.text = helloWorld.message
            }
        }.start()
 */
        viewLifecycleOwner.lifecycleScope.launch {
//            val api = ApiService.create(requireContext())

//            val qidDateFormat = SimpleDateFormat("yyyy-MM-dd")
//            val qid = qidDateFormat.format(Date())
//            val question = api.getQuestion(qid)
            // '질문 가져오기' API가 Question을 직접 반환하기 않고 Response<Question>을 반환하도록 변경했습니다.
            val questionResponse = api.getQuestion(LocalDate.now())
            // Response의 isSuccessful로 성공 여부를 확인한 후 body() 메서드로 Question을 가져옵니다.
            if (questionResponse.isSuccessful) {
                question = questionResponse.body()!!

                val dateFormatter = DateTimeFormatter.ofPattern("yyyy. M. d.")

                binding.date.text = dateFormatter.format(question!!.id)
                binding.question.text = question!!.text

                // WriteActivity에서 답을 쓴 후 TodayFragment로 돌아왔을 때 카드에 답이 표시되도록 서버에서 답을 받아와 답의 유무에 따라
                // answerArea의 visibility를 변경하고 답을 textAnswer에 표시합니다.
//                val answer = api.getAnswer(question!!.id).body()
//                binding.answerArea.isVisible = answer != null
//                binding.textAnswer.text = answer?.text

                // 글쓰기 버튼은 answerArea와 반대로 답이 없으면 표시되고 답이 있으면 숨겨져야 합니다.
//                binding.writeButton.isVisible = answer == null

                setupAnswer()
            }
//            val dateFormatter = DateTimeFormatter.ofPattern("yyyy. M. d.")

//            val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.KOREA)
            // parse() 메서드로는 API의 응답으로 10자리 날짜 문자열을 Date 객체로 변환합니다.
//            binding.date.text = dateFormat.format(qidDateFormat.parse(question.id))
//            binding.date.text = dateFormatter.format(question.id)
//            binding.question.text = question.text
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    fun showDeleteConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.dialog_msg_are_you_sure_to_delete)
            .setPositiveButton(R.string.ok) { dialog, which ->
                lifecycleScope.launch {
                    val deleteResponse = api.deleteAnswer(question!!.id)
                    if (deleteResponse.isSuccessful) {
                        binding.answerArea.isVisible = false
                        binding.writeButton.isVisible = true
                    }
                }
            }
            .setNegativeButton(R.string.cancel) { dialog, which ->

            }.show()
    }

    suspend fun setupAnswer() {
        val question = question ?: return

        val answer = api.getAnswer(question.id).body()
        binding.answerArea.isVisible = answer != null
        binding.textAnswer.text = answer?.text

        binding.writeButton.isVisible = answer == null

        // 이미지 확대, 축소 기능은 TodayFragment를 만든 후 같이 확인하겠습니다.
        // TodayFragment에서는 answer가 있을 때 photoAnswer에 이미지를 표시하고, 터치했을 때
        // ImageViewerActivity를 URL에 전달하며 시작합니다.
        binding.photoAnswer.isVisible = !answer?.photo.isNullOrEmpty()
        Log.d("TodayFragment", "${answer?.photo}")
        answer?.photo?.let {
            binding.photoAnswer.load(it) {
//            binding.photoAnswer.load("http:/192.168.1.169:8080/v2/images/3fb0b977a5504eb28151c490fc5b2926") {
                placeholder(R.drawable.ph_image)
            }
            binding.photoAnswer.setOnClickListener {
                startActivity(Intent(requireContext(), ImageViewerActivity::class.java).apply {
                    putExtra(ImageViewerActivity.EXTRA_URL, answer.photo)
//                    putExtra(ImageViewerActivity.EXTRA_URL, "http:/192.168.1.169:8080/v2/images/3fb0b977a5504eb28151c490fc5b2926")
                })
            }
        }
    }
}