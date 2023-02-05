package kr.co.bullets.dailyq.ui.profile

import android.content.Intent
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import kr.co.bullets.dailyq.R
import kr.co.bullets.dailyq.api.response.QuestionAndAnswer
import kr.co.bullets.dailyq.databinding.ItemUserAnswerCardBinding
import kr.co.bullets.dailyq.ui.details.DetailsActivity
import kr.co.bullets.dailyq.ui.image.ImageViewerActivity
import java.time.format.DateTimeFormatter

// 레이아웃 파일에서 자동으로 생성된 ItemUserAnswerCardBinding과 연결할 뷰 홀더를 추가합니다.
// 사진을 터치해 확대해서 볼 수 있고, 카드를 터치하면 상세보기 화면으로 이동합니다.
class UserAnswerViewHolder(val binding: ItemUserAnswerCardBinding) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. M. d.")
    }

    fun bind(item: QuestionAndAnswer) {
        val question = item.question
        val answer = item.answer

        binding.date.text = DATE_FORMATTER.format(question.id)

        binding.question.text = question.text
        binding.textAnswer.text = answer.text
        answer.photo?.let {
            binding.photoAnswer.load(it) {
                placeholder(R.drawable.ph_image)
                error(R.drawable.ph_image)
            }
        }
        binding.textAnswer.isVisible = !answer.text.isNullOrEmpty()
        binding.photoAnswer.isVisible = !answer.photo.isNullOrEmpty()

        binding.photoAnswer.setOnClickListener {
            val context = itemView.context
            context.startActivity(Intent(context, ImageViewerActivity::class.java).apply {
                putExtra(ImageViewerActivity.EXTRA_URL, answer.photo)
            })
        }
        binding.root.setOnClickListener {
            val context = itemView.context
            context.startActivity(Intent(context, DetailsActivity::class.java).apply {
                putExtra(DetailsActivity.EXTRA_QID, question.id)
            })
        }
    }
}