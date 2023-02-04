package kr.co.bullets.dailyq.ui.timeline

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import kr.co.bullets.dailyq.R
import kr.co.bullets.dailyq.api.response.Question
import kr.co.bullets.dailyq.databinding.ItemTimelineCardBinding
import kr.co.bullets.dailyq.ui.details.DetailsActivity
import java.time.format.DateTimeFormatter

// 다음으로 RecyclerView에서 사용할 TimelineCardViewHolder를 만듭니다.
// [코드 8-6]의 TimelineCardViewHolder는 bind() 메서드에서 Question을 매개변수로 받아 레이아웃 파일에서 생성된 ItemTimelineCardBinding을 채웁니다.
// Question.id를 날짜로 변환할 때 DateTimeFormatter를 이용하는데, 매번 생성할 필요가 없기 때문에 성능을 위해
// 모든 TimeLineCardViewHolder에서 공유하도록 companion object에 선언했습니다.
// 카드를 터치했을 때 상세 화면으로 이동하는 것은 OnClickListener만 등록하고 상세 화면을 만든 후에 구현하겠습니다.
class TimelineCardViewHolder(val binding: ItemTimelineCardBinding) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. M. d.")
    }

    fun bind(question: Question) {
        binding.date.text = DATE_FORMATTER.format(question.id)
        binding.question.text = if (question.answerCount > 0) {
            binding.root.context.getString(R.string.answer_count_format, question.answerCount)
        } else {
            binding.root.context.getString(R.string.no_answer_yet)
        }

        binding.card.setOnClickListener {
            // TODO 상세 화면으로 이동
            // 마지막으로 타임라인에서 상세보기를 시작할 수 있게 TimelineCardViewHolder에 비워덨던 OnClickListener를 구현합니다.
            val context = binding.root.context

            context.startActivity(Intent(context, DetailsActivity::class.java).apply {
                putExtra(DetailsActivity.EXTRA_QID, question.id)
            })
        }
    }
}