package kr.co.bullets.dailyq.ui.profile

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kr.co.bullets.dailyq.api.ApiService
import kr.co.bullets.dailyq.api.response.QuestionAndAnswer
import java.time.LocalDate

// 사용자의 답도 타임라인처럼 나눠서 가져올 수 있도록 PagingSource를 확장해 [코드 10-18]의 UserAnswerPagingSource를 만듭니다.
class UserAnswerPagingSource(val api: ApiService, val uid: String) : PagingSource<LocalDate, QuestionAndAnswer>() {

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, QuestionAndAnswer> {
        val userAnswersResponse = api.getUserAnswers(uid, params.key)

        return if (userAnswersResponse.isSuccessful) {
            val userAnswers = userAnswersResponse.body()!!

            val nextKey = if (userAnswers.isNotEmpty()) {
                userAnswers.minOf { it.question.id }
            } else {
                null
            }
            LoadResult.Page(data = userAnswers, prevKey = null, nextKey = nextKey)
        } else {
            LoadResult.Error(Throwable("Paging Error"))
        }
    }

    override fun getRefreshKey(state: PagingState<LocalDate, QuestionAndAnswer>): LocalDate? = null
}