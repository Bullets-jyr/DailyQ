package kr.co.bullets.dailyq.ui.timeline

import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import kr.co.bullets.dailyq.databinding.ItemTimelineLoadStateBinding

// [코드 8-11]의 TimelineLoadStateViewHolder에서는 재시도 버튼을 클릭했을 때 실행될 함수를 생성자로 받고,
// bind() 메서드에서 [코드 8-12] 어댑터의 onBindViewHolder() 메서드로 전달되는 LoadState에 따라 버튼은
// 프로그래스바와 재시도 버튼을 선택적으로 표시합니다.
class TimelineLoadStateViewHolder(val binding: ItemTimelineLoadStateBinding, val retry: () -> Unit) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.retry.setOnClickListener {
            retry()
        }
    }

    fun bind(loadState: LoadState) {
        binding.progress.isVisible = loadState is LoadState.Loading
        binding.retry.isVisible = loadState is LoadState.Error
    }
}