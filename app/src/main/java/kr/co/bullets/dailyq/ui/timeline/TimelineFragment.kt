package kr.co.bullets.dailyq.ui.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kr.co.bullets.dailyq.databinding.FragmentTimelineBinding
import kr.co.bullets.dailyq.ui.base.BaseFragment

// [코드 8-8]과 [코드 8-9]의 TimelineFragment에서는 PagingSource와 PagingDataAdapter의 사이에 있는 Pager를 만들고, RecyclerView에 어댑터를 연결합니다.
class TimelineFragment : BaseFragment() {

    var _binding: FragmentTimelineBinding? = null
    val binding
        get() = _binding!!
    lateinit var adapter: TimelineAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            adapter = TimelineAdapter(requireContext())
            recycler.adapter = adapter
            recycler.layoutManager = LinearLayoutManager(context)
        }

        lifecycleScope.launch {
            // Pager를 생성하기 위해 페이지의 구성을 정의하는 PagingConfig, 최초 로딩에서 사용할 키,
            // 그리고 API와 데이터베이스를 함께 사용할 때 중재하는 RemoteMediator, 데이터를 공급하는 PagingSource를 만드는 팩토리 함수를 생성자로 전달합니다.
            // 8장의 타임라인은 API에서만 데이터를 가져오기 때문에 RemoteMediator는 사용하지 않습니다.
            // 10장에서 로컬 데이터베이스를 사용할 때 다시 더 알아보겠습니다.
            // TimelineFragment에서는 PagingSource와 PagingConfig로 Pager를 생성하고, Pager가 노출하는 PagingData의 Flow를 사용해
            // PagingDataAdapter에 데이터를 제공합니다.
            // PagingConfig에서 initialLoadSize는 처음 요청하는 페이지의 크기입니다.
            // 이후에 리스트를 스크롤해 데이터를 더 불러올 땐 pageSize를 사용합니다.
            // initialLoadSize와 pageSize는 모두 PagingSource.load() 메서드의 매개변수인 LoadParams.loadSize로 전달됩니다.
            // Paging 라이브러리는 데이터를 불러오기 전에 [그림 8-10]처럼 플레이스홀더로 미리 자리를 표시할 수 있습니다.
            // 플레이스홀더 기능을 사용하면 [그림 8-4]처럼 특정 위치에서 다음 데이터를 불러올 때까지 대기하지 않고 자연스럽게 스크롤을 하면서 사용할 수 있습니다.
            // 플레이스홀더 기능을 사용하려면 PagingConfig의 enablePlaceholders가 true여야 하고, PagingSource가 불러올 수 있는 데이터의 개수를 알 수 있어야 합니다.
            // 불러올 수 있는 데이터의 개수의 전달은 PagingSource에서 load() 메서드의 결과로 반환하는 LoadParams.Page에 itemsBefore, itemsAfter로 할 수 있습니다.
            // 플레이스홀더는 주로 전체 개수를 알 수 있고 변경이 상대적으로 적은 로컬 데이터베이스에서 데이터를 불러올 때 사용합니다.
            // 플레이스홀더를 사용할 때 주의할 점은 실제 데이터를 불러오기 전 플레이스홀더가 표시됐을 때
            // PagingDataAdapter의 onBindViewHolder()에서 getItem()으로 데이터에 접근하면 null을 반환한다는 것입니다.
            // [코드 8-7]의 onBindViewHolder()에서는 null이 아닌 경우에만 UI를 갱신하도록 safe-call 연산자(?.)를 사용했습니다.
            Pager(PagingConfig(initialLoadSize = 6, pageSize = 3, enablePlaceholders = false)) {
                TimelinePagingSource(api)
            }.flow.collectLatest {
                adapter.submitData(it)
            }
        }
    }
}