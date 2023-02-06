package kr.co.bullets.dailyq.ui.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.delay
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

        // [코드 8-13]에서 이 어댑터를 사용하도록 TimelineFragment를 수정합니다.
        // 어댑터를 설정하는 기존의 코드를 제거하고 withLoadStateFooter() 메서드로 TimelineAdapter에
        // TimelineLoadStateAdapter를 결합해서 만들어진 ConcatAdapter를 RecyclerView로 설정하는 것만으로 모든 작업이 끝납니다.
        binding.apply {
            adapter = TimelineAdapter(requireContext())
//            recycler.adapter = adapter

            // TimelineAdapter에 LoadStateListener를 등록해 mediator가 갱신 상태가 아닌 경우엔
            // binding.refreshLayout.isRefreshing을 false로 변경해 새로고침 아이콘을 숨겼습니다.
            adapter.addLoadStateListener {
                if (it.mediator?.refresh is LoadState.NotLoading) {
                    binding.refreshLayout.isRefreshing = false
                }
            }
            recycler.adapter = adapter.withLoadStateFooter(TimelineLoadStateAdapter {
                adapter.retry()
            })
            recycler.layoutManager = LinearLayoutManager(context)

            // [코드 10-41]에서는 SwipeRefreshLayout을 당겼다 놓았을 때 갱신하는 로직을 만듭니다.
            // SwipeRefreshLayout을 당겼다 놓으면 OnRefreshListener의 OnRefresh() 메서드가 호출됩니다.
            // 여기에서 adapter.refresh() 메서드를 호출해 갱신을 요청하면 TimelineRemoteMediator의 load() 메서드가 호출되고
            // LoadType으로 REFRESH가 전달됩니다.
            binding.refreshLayout.setOnRefreshListener {
                lifecycleScope.launch {
                    // 그리고 새로고침이 너무 빨라 SwipeRefreshLayout의 진행 상태 표시를 보기가 힘들어 테스트를 위해
                    // adapter.refresh()를 호출하기 전에 delay 함수로 1초의 지연을 추가했습니다.
                    // 앱을 실행하고 타임라인 화면을 드래그해서 내리면 상단에 로딩 프로그래스바가 표시되고,
                    // 충분히 당긴 후 손을 떼면 서버에서 새로 데이터를 불러오는 것을 볼 수 있습니다.
                    delay(1000)
                    adapter.refresh()
                }
            }
        }

        // 마지막으로 Pager를 생성할 때 QuestionDao에서 만든 PagingSource와 TimelineRemoteMediator를 사용하도록 수정합니다.
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
//            Pager(PagingConfig(initialLoadSize = 6, pageSize = 3, enablePlaceholders = false)) {
//                TimelinePagingSource(api)
//            }.flow.collectLatest {
//                adapter.submitData(it)
//            }
            @OptIn(ExperimentalPagingApi::class)
            Pager(PagingConfig(initialLoadSize = 6, pageSize = 3, enablePlaceholders = false), null, TimelineRemoteMediator(api, db)) {
                db.getQuestionDao().getPagingSource()
            }.flow.collectLatest {
                adapter.submitData(it)
            }
        }
    }
}