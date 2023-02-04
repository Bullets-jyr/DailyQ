package kr.co.bullets.dailyq.ui.timeline

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kr.co.bullets.dailyq.api.ApiService
import kr.co.bullets.dailyq.api.response.Question
import java.time.LocalDate

// [그림 8-8]에서 본 것처럼 PagingSource는 페이징 라이브러리에서 API나 로컬 데이터베이스에서 데이터를 불러오는 역할을 합니다.
// TimelinePagingSource는 PagingSource를 상속받아 '질문 목록 가져오기' API를 호출할 때 사용하는 키의 타입인 LocalDate와
// 결과로 가져올 데이터의 타입인 Question을 타입 매개변수로 전달합니다.
// 생성자에서는 API를 호출하기 위해 ApiService를 전달받습니다.
// TimelinePagingSource는 두 개의 메서드를 오버라이드 해야 합니다.
class TimelinePagingSource(val api: ApiService) : PagingSource<LocalDate, Question>() {

    // load() 메서드는 LoadParams를 인자로 받아 조건에 따라 데이터를 불러옵니다.
    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Question> {
        val fromDate = params.key ?: LocalDate.now()

        val questionResponse = api.getQuestions(fromDate, params.loadSize)

        // 데이터를 불러오는 데 성공하면 LoadResult.Page에 불러온 데이터와 다음 페이지를 불러올 때 사용할 키를 담아 반환하고,
        if (questionResponse.isSuccessful) {
            val questions = questionResponse.body()!!

            if (questions.isNotEmpty()) {
                val oldest = questions.minOf { it.id }
                val nextKey = oldest.minusDays(1)

                return LoadResult.Page(data = questions, prevKey = null, nextKey = nextKey)
            }
            return LoadResult.Page(data = questions, prevKey = null, nextKey = null)
        }
        // 데이터를 불러오느데 실패했다면 LoadResult.Error에 오류 이유를 담아 반환합니다.
        return LoadResult.Error(Throwable("Paging Error"))
    }

    // getRefreshKey() 메서드는 PagingState를 받아 페이징 데이터를 갱신할 때 사용할 키를 반환합니다.
    // null을 반환하면 load() 메서드에서 기본값을 사용합니다.
    override fun getRefreshKey(state: PagingState<LocalDate, Question>): LocalDate? = null
}