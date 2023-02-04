package kr.co.bullets.dailyq.ui.timeline

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import kr.co.bullets.dailyq.api.response.Question
import kr.co.bullets.dailyq.databinding.ItemTimelineCardBinding

// [코드 8-7]의 TimelineAdapter는 RecyclerView.Adapter를 확장한 PagingDataAdapter를 상속받습니다.
// PagingDataAdapter는 Pager가 불러온 데이터를 백그라운드에서 가공해 불러옵니다.
// 로딩 상태를 addLoadStateListener() 메서드나 withLoadStateHeader(), withLoadStateFooter(), withLoadStateHeaderAndFooter() 메서드들로 수신할 수 있습니다.
// 그리고 로딩에 실패했을 때 retry() 메서드로 다시 요청하거나 refresh() 메서드로 데이터를 새로고침 할 수 있습니다.
// TimelineAdapter는 일반적인 RecyclerView.Adapter를 상속받아 구현하는 것과 큰 차이가 없습니다.
// 차이점은 단지 PagingDataAdapter의 생성자로 데이터가 변경됐는지 비교하는 DiffUtil.ItemCallback을 전달하는 것과
// 아이템을 가져올 때 onBindViewHolder() 메서드에서 볼 수 있는 getItem() 메서드로 데이터를 가져온다는 것입니다.
class TimelineAdapter(val context: Context) : PagingDataAdapter<Question, TimelineCardViewHolder>(QuestionComparator) {

    object QuestionComparator: DiffUtil.ItemCallback<Question>() {
        override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem == newItem
        }
    }

    val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineCardViewHolder {
        return TimelineCardViewHolder(ItemTimelineCardBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: TimelineCardViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }
}