package kr.co.bullets.dailyq.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.bullets.dailyq.AuthManager
import kr.co.bullets.dailyq.R
import kr.co.bullets.dailyq.databinding.ActivityLoginBinding
import kr.co.bullets.dailyq.ui.base.BaseActivity
import kr.co.bullets.dailyq.ui.main.MainActivity

class LoginActivity : BaseActivity() {

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.password.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    login()
                    return@setOnEditorActionListener true
                }
                EditorInfo.IME_ACTION_UNSPECIFIED -> {
                    if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                        login()
                        return@setOnEditorActionListener true
                    }
                }
            }
            false
        }

        binding.login.setOnClickListener {
            login()
        }
    }

    // 아이디와 패스워드 검사는 validateUidAndPassword() 메서드에서 합니다.
    fun validateUidAndPassword(uid: String, password: String): Boolean {
        binding.userIdLayout.error = null
        binding.passwordLayout.error = null

        // 먼저 입력한 아이디와 패스워드의 길이를 검사하고 TextInputLayout인 userIdLayout과 passwordLayout의 error 속성으로 에러 메시지를 표시했습니다.
        if (uid.length < 5) {
            binding.userIdLayout.error = getString(R.string.error_uid_too_short)
            return false
        }

        if (password.length < 8) {
            binding.passwordLayout.error = getString(R.string.error_password_too_short)
            return false
        }

        // 그다음으로 '숫자'에 해당하는 정규표현식을 사용해 패스워드를 검사했습니다.
        // 정규 표현식은 특정한 규칙의 문자열을 표현하는 데 사용되는 형식 언어입니다.
        // 메신저나 SNS에 글을 썼을 때 이메일, 전화번호, URL 등에 자동으로 링크가 만들어지는 기능이 정규 표현식을 사용하는 대표적인 예입니다.
        // 여기서는 간단히 숫자의 포함 여부만 검사했지만 실제 서비스에서는 '숫자, 소문자, 대문자, 특수문자로 구성된 8~20자'와 같이 복잡한 규칙을
        // 다음과 같은 복잡한 정규 표현식으로 만들어 사용합니다.
        // val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()]).{8,20}$".toRegex()
        val numberRegex = "[0-9]".toRegex()
        if (!numberRegex.containsMatchIn(password)) {
            binding.passwordLayout.error = getString(R.string.error_password_must_contain_number)
            return false
        }

        // val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()]).{8,20}$".toRegex()

        return true
    }

    // LoginActivity.login() 매서드에서는 ApiService.login() 메서드를 호출하기 전에 프로그래스바를 다시 숨깁니다.
    // 그리고 중복으로 호출하는 것을 방지하기 위해 LoginActivity.login() 메서드에 진입했을 때
    // 프로그래스바가 표시된 상태면 더 이상 진행하지 않고 반환합니다.
    // 토큰을 AuthManager에 저장한 후에는 MainActivity를 시작하고 LoginActivity를 종료합니다.
    fun login() {
        if (binding.progress.isVisible) {
            return
        }

        val uid = binding.userId.text?.trim().toString()
        val password = binding.password.text?.trim().toString()

        if (validateUidAndPassword(uid, password)) {
            binding.progress.isVisible = true

            /**
             * 로그인 액티비티에서는 사용자 아이디와 패스워드가 유효한지 검사하고 '토큰 발급/갱신' API를 호출해 토큰을 받아옵니다.
             * 그리고 토큰을 AuthManager에 저장하고 MainActivity를 시작합니다.
             */
            lifecycleScope.launch {
                val authTokenResponse = api.login(uid, password)
                if (authTokenResponse.isSuccessful) {
                    val authToken = authTokenResponse.body()

                    AuthManager.uid = uid
                    AuthManager.accesToken = authToken?.accesToken
                    AuthManager.refreshToken = authToken?.refreshToken

                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    binding.progress.isVisible = false
                    Toast.makeText(this@LoginActivity, R.string.error_login_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
