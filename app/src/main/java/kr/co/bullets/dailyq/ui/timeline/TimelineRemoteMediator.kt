package kr.co.bullets.dailyq.ui.timeline

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import kr.co.bullets.dailyq.api.ApiService
import kr.co.bullets.dailyq.db.AppDatabase
import kr.co.bullets.dailyq.db.entity.QuestionEntity
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.LocalDate

// 타임라인은 원격의 서버와 로컬 데이터베이스, 두 개의 데이터 소스를 사용합니다.
// Room에서 PagingSource를 만들어주고, Pager는 Room에서 데이터를 불러와 표시하기 때문에
// [그림 8-9]처럼 서버에서 데이터를 가져와 Room에 채워 넣는 RemoteMediator가 필요합니다.
// [코드 10-35]를 보고 RemoteMediator를 확장하는 TimelineRemoteMediator를 만듭니다.
// 이에 따라 RemoteMediator의 initialize()와 load() 메서드를 재정의해야합니다.
@OptIn(ExperimentalPagingApi::class)
class TimelineRemoteMediator(val api: ApiService, val db: AppDatabase) : RemoteMediator<Int, QuestionEntity>() {

    // initialize() 메서드는 시작할 때 호출되고 데이터의 갱신 필요 여부를 반환합니다.
    // 로컬 데이터베이스에 보관 중인 데이터가 만료돼 갱신이 필요한 경우 InitializeAction.LAUNCH_INITIAL_REFRESH를 반환하고,
    // 로컬 데이터베이스의 데이터를 그대로 사용해도 되는 경우에는 InitializeAction.SKIP_INITIAL_REFRESH를 반환합니다.
    override suspend fun initialize(): InitializeAction {
        return InitializeAction.SKIP_INITIAL_REFRESH
    }

    // load() 메서드는 Pager가 로컬 데이터베이스의 PagingSource에서 데이터를 불러오다가 데이터가 없는 경계에 도달해
    // 원격 데이터 소스에 추가 데이터를 요청해야 하는 경우 호출됩니다.
    // load() 메서드는 두 개의 매개변수를 갖습니다. 새로고침을 위한 것인지, 목록의 앞이나 뒷부분의 데이터를 불러오기 위한 것인지를
    // 알려주는 LoadType과 페이징 설정, 불러온 페이지 등을 가지고 있는 PagingState입니다.
    // 원격에서 데이터를 요청해 로컬 데이터베이스를 갱신한 후 결과에 따라 성공과 실패 혹은 더 불러올 데이터가 있는지를 알려주는 MediatorResult를 반환합니다.
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, QuestionEntity>
    ): MediatorResult {
        val pageSize = state.config.pageSize
        val today = LocalDate.now()

        // [코드 10-35]의 load() 메서드는 loadType에 따라 요청할 질문 목록의 시작 날짜를 계산하고 서버에서 받은 데이터를 Room에 저장한 후 Pager에 결과를 반환합니다.
        val fromDate = when (loadType) {
            // LoadType.REFRESH면 모든 데이터를 삭제하고 오늘 날짜의 데이터부터 받아오면 됩니다.
            LoadType.REFRESH -> {
                today
            }
            // LoadType.PREPEND면 [그림 10-6]처럼 목록의 앞부분 데이터를 불러와야 합니다.
            LoadType.PREPEND -> {
                // 이전에 불러온 데이터의 가장 앞부분은 PagingState.firstItemOrNull() 메서드로 가져올 수 있습니다.
                // 이 날짜에서 페이지 크기만큼 이동해 서버에 요청합니다.
                val firstItem = state.firstItemOrNull()

                // 처음 목록을 표시해 불러 온 데이터가 없어 firstItem이 null인 경우이거나
                if (firstItem == null) {
                    return MediatorResult.Success(endOfPaginationReached = false)
                }

                // 이미 모두 DB에 있어 firstItem의 id가 서버에서 데이터를 불러올 필요가 없는 경우에는 바로 MediatorResult.Success(endOfPaginationReached = true 또는 false)를 반환합니다.
                // endOfPaginationReached가 true면 Pager는 해당 LoadType에 대해서 더 이상 load() 메서드를 호출하지 않습니다.
                if (firstItem.id >= today) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                } else {
                    firstItem.id.plusDays(pageSize.toLong())
                    val prevKey = firstItem.id
                    if (prevKey > today) {
                        today
                    } else {
                        prevKey
                    }
                }
            }
            // LoadType.APPEND에서는 목록의 다음 데이터를 불러오는데 마지막 데이터에서 하루 전의 날짜를 fromDate로 사용합니다.
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                if (lastItem == null) {
                    today
                } else {
                    lastItem.id.minusDays(1)
                }
            }
        }

        try {
            val questions = api.getQuestions(fromDate, pageSize).body()
            // REFRESH에서 endOfPaginationReached가 true면 모든 데이터를 불러온 것으로 보고 PREPEND와 APPEND도 호출하지 않습니다.
            val endOfPaginationReached = questions.isNullOrEmpty()

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    db.getQuestionDao().deleteAll()
                }

                questions?.map {
                    QuestionEntity(it.id, it.text, it.answerCount, it.updatedAt, it.createdAt)
                }?.let {
                    db.getQuestionDao().insertOrReplace(it)
                }
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: SocketTimeoutException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }
}