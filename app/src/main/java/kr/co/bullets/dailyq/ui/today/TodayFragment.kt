package kr.co.bullets.dailyq.ui.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.bullets.dailyq.api.ApiService
import kr.co.bullets.dailyq.databinding.FragmentTodayBinding
import kr.co.bullets.dailyq.ui.base.BaseFragment
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class TodayFragment : BaseFragment() {

    var _binding: FragmentTodayBinding? = null
    val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

            val qidDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val qid = qidDateFormat.format(Date())
            val question = api.getQuestion(qid)

            val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.KOREA)
            // parse() 메서드로는 API의 응답으로 10자리 날짜 문자열을 Date 객체로 변환합니다.
            binding.date.text = dateFormat.format(qidDateFormat.parse(question.id))
            binding.question.text = question.text
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}