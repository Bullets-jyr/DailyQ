package kr.co.bullets.dailyq.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import coil.load
import coil.transform.CircleCropTransformation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kr.co.bullets.dailyq.AuthManager
import kr.co.bullets.dailyq.R
import kr.co.bullets.dailyq.api.response.User
import kr.co.bullets.dailyq.databinding.FragmentProfileBinding
import kr.co.bullets.dailyq.db.entity.UserEntity
import kr.co.bullets.dailyq.ui.base.BaseFragment
import java.net.ConnectException

class ProfileFragment : BaseFragment() {

    companion object {
        const val ARG_UID = "uid"
    }

    var _binding: FragmentProfileBinding? = null
    val binding
        get() = _binding!!
    val uid: String by lazy {
        requireArguments().getString(ARG_UID)!!
    }

    var adapter: UserAnswerAdapter? = null

    val userAnswerFlow = Pager(PagingConfig(pageSize = 5)) {
        UserAnswerPagingSource(api, uid)
    }.flow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 앞에서는 API에서 응답을 받은 후 사용자 정보를 표시했지만,
        // 개선된 버전에선 데이터베이스에서 사용자 정보를 불러와 표시하면서 동시에 서버로 사용자 정보를 요청합니다.
        // 네트워크가 연결되지 않거나 느리더라도 빠르게 사용자 정보를 표시할 수 있고,
        // 만약 로컬의 정보가 오래됐다면 서버의 응답을 받은 후에 새로운 정보로 갱신해 표시합니다.
        // [코드 10-29]를 보고 ProfileFragment를 수정합니다.
        // API에서 받은 User가 아니라 데이터베이스에서 가져온 UserEntity를 사용해 프로필을 표시하기 위해
        // setupUserProfile() 메서드의 매개변수 타입을 User에서 UserEntity로 변경합니다.
        // onViewCreated() 메서드에서 사용자 정보를 불러와 표시하는 코드도 모두 변경합니다.

        // HTTP 캐시를 사용할 때와 다른 점은 DB에 저장된 사용자 정보는 영구적으로 보관이 된다는 것과
        // API의 캐시 지원 여부에 상관없다는 것, API의 응답을 받기 전에 화면에 표시 할 수 있다는 것입니다.
        // 데이터베이스에 저장된 정보가 이미 변경되어 오래된 정보일 수 있지만, 사용자 프로필은 오래된 정보라도 보여주는 것이
        // 빈 화면을 보여주는 것보다 낫습니다. 하지만 오래된 정보가 오류로 느껴지는 경우도 있으니 사용자에게 보이는 부분은 혼란이 없도록
        // 기획자에게 알려 명확히 하는 것이 좋습니다.
        lifecycleScope.launch {
            // 1) 데이터베이스에서 사용자 정보를 불러옵니다
            val oldUserEntity = db.getUserDao().get(uid)
            if (oldUserEntity != null) {
                // 2) 사용자 정보가 있으면 화면에 표시합니다.
                setupProfile(oldUserEntity)
            }

            val user: User

            try {
                // 3) 서버에서 사용자 정보를 가져옵니다.
                val userResponse = api.getUser(uid)
                if (!userResponse.isSuccessful) {
                    return@launch
                }

//                val user = userResponse.body() ?: return@launch
//                setupProfile(user)
                user = userResponse.body()!!
            } catch (e: ConnectException) {
                Log.e("ProfileFragment", "$e")
                return@launch
            }

            val newUserEntity = UserEntity(
                user.id,
                user.name,
                user.description,
                user.photo,
                user.answerCount,
                user.followerCount,
                user.followingCount,
                user.isFollowing ?: false,
                user.updatedAt
            )

            // 4) 데이터베이스를 갱신하고 화면에 표시합니다.
            if (oldUserEntity == null) {
                db.getUserDao().insert(newUserEntity)
                setupProfile(newUserEntity)
            } else if (oldUserEntity != newUserEntity) {
                db.getUserDao().update(newUserEntity)
                setupProfile(newUserEntity)
            }
        }

        lifecycleScope.launch {
            adapter = UserAnswerAdapter(requireContext())
            binding.pager.adapter = adapter

            userAnswerFlow.collectLatest {
                adapter?.submitData(it)
            }
        }
    }

    // 앱을 실행하여 팔로우/언팔로우 버튼을 터치해 팔로우 상태가 변경되는 것을 확인하고,
    // 백버튼으로 화면을 벗어났다가 돌아와도 변경된 상태가 제대로 표시되는 것을 확인합니다.
    fun setupProfile(user: UserEntity) {
        binding.name.text = user.name
        binding.description.text = user.description
        binding.answerCount.text = user.answerCount.toString()
        binding.followerCount.text = user.followerCount.toString()
        binding.followingCount.text = user.followingCount.toString()
        user.photo?.let {
            binding.photo.load(it) {
                placeholder(R.drawable.ph_user)
                error(R.drawable.ph_user)
                transformations(CircleCropTransformation())
            }
        }

        when {
            user.id == AuthManager.uid -> {
                binding.followButton.setText(R.string.me)
                binding.followButton.isEnabled = false
            }
            user.isFollowing == true -> {
                binding.followButton.setText(R.string.unfollow)
                binding.followButton.isEnabled = true
                binding.followButton.backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.unfollow_button)
                binding.followButton.setOnClickListener {
                    lifecycleScope.launch {
                        val response = api.unfollow(user.id)
                        if (response.isSuccessful) {
                            setupProfile(user.copy(isFollowing = false))
                        }
                    }
                }
            }
            else -> {
                binding.followButton.setText(R.string.follow)
                binding.followButton.isEnabled = true
                binding.followButton.backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.follow_button)
                binding.followButton.setOnClickListener {
                    lifecycleScope.launch {
                        val response = api.follow(user.id)
                        if (response.isSuccessful) {
                            setupProfile(user.copy(isFollowing = true))
                        }
                    }
                }
            }
        }
    }
}