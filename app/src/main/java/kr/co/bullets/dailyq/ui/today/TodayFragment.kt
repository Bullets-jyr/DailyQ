package kr.co.bullets.dailyq.ui.today

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import kr.co.bullets.dailyq.api.response.HelloWorld
import kr.co.bullets.dailyq.databinding.FragmentTodayBinding
import kr.co.bullets.dailyq.ui.base.BaseFragment
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DateFormat
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
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}