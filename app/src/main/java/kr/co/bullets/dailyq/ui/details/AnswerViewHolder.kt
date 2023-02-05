package kr.co.bullets.dailyq.ui.details

import android.content.Intent
import android.text.format.DateUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import kr.co.bullets.dailyq.R
import kr.co.bullets.dailyq.api.response.Answer
import kr.co.bullets.dailyq.databinding.ItemAnswerBinding
import kr.co.bullets.dailyq.ui.image.ImageViewerActivity
import kr.co.bullets.dailyq.ui.profile.ProfileActivity

// online.dailyq.ui.details 패키지를 추가하고 [코드 9-7]의 AnswerViewHolder를 만듭니다.
// 사용자가 답을 쓴 시간은 경과 시간으로 표시하는데, DataUtils에 준비되어 있는 getRelativeTimeString() 메서드를 사용합니다.
// getRelativeTimeString() 메서드는 매개변수로 전달되는 시간에서부터 현재 시간까지 얼마나 경과했는지 경과 시간에 따라
// 'n분 전', '어제', '그저께', 'n일 전', 'yyyy년 MM월 dd일' 등의 표현으로 반환합니다.
// 만약 기기의 언어 설정이 영어라면 'n minutes ago', 'Yesterday'등으로 반환합니다.
class AnswerViewHolder(val binding: ItemAnswerBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(answer: Answer) {
        binding.userName.text = answer.answerer?.name

        if (!answer.answerer?.photo.isNullOrBlank()) {
            binding.userPhoto.load(answer.answerer?.photo) {
                placeholder(R.drawable.ph_user)
                error(R.drawable.ph_user)
                transformations(CircleCropTransformation())
            }
        }

        binding.userPhoto.setOnClickListener {
            val context = itemView.context
            context.startActivity(Intent(context, ProfileActivity::class.java).apply {
                putExtra(ProfileActivity.EXTRA_UID, answer.answerer?.id)
            })
        }

        binding.textAnswer.text = answer.text
        binding.textAnswer.isVisible = !answer.text.isNullOrEmpty()
        binding.photoAnswer.load(answer.photo) {
            placeholder(R.drawable.ph_image)
            error(R.drawable.ph_image)
        }
        binding.photoAnswer.isVisible = !answer.photo.isNullOrEmpty()
        binding.photoAnswer.setOnClickListener {
            val context = itemView.context
            context.startActivity(Intent(context, ImageViewerActivity::class.java).apply {
                putExtra(ImageViewerActivity.EXTRA_URL, answer.photo)
            })
        }

        binding.elapsedTime.text = DateUtils.getRelativeTimeSpanString(answer.createdAt.time)
    }
}