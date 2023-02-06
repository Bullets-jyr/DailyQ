package kr.co.bullets.dailyq.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kr.co.bullets.dailyq.db.entity.QuestionEntity

// [코드 10-32]에서 QuestionEntity를 다룰 QuestionDao를 만듭니다.
// 서버에서 가져온 데이터는 항상 로컬에 저장된 데이터보다 최신이므로 기본키의 충돌이 발생할 경우 서버의 데이터로 교체하면 됩니다.
// 질문을 데이터베이스에 저장하는 insertOrReplace() 메서드는 @Insert 어노테이션의 onConflict 속성을 OnConflictStrategy.REPLACE로 지정합니다.
// 서버에서 데이터를 가져와 Room에 저장하는 역할은 RemoteMediator에서 하는데 이에 대해서는 Room 설정을 마친 후에 알아보겠습니다.
// getPagingSource() 메서드는 PagingSource()를 반환합니다.
// 8장에서 서버에서 데이터를 나눠서 가져오는 TimelinePagingSource를 직접 만들었는데, Room은 PagingSource를 반환 타입으로 지정하면
// Room에서 데이터를 나눠 가져오는 PagingSource를 자동으로 생성합니다.
// 기존에 사용하던 TimelinePagingSource는 더 이상 사용하지 않습니다.
@Dao
interface QuestionDao {
    // 로컬 데이터베이스에 저장한 질문을 갱신하는 또 다른 시점은 상세보기에 진입할 때입니다.
    // '질문 목록 가져오기' API가 아니라 '질문 가져오기' API에서 받은 정보로 로컬 데이터베이스의 개별 질문을 갱신합니다.
    // QuestionDao에 하나의 Entity를 가져올 수 있는 get() 메서드와 하나의 Entity를 추가할 수 있는 insertOrReplace() 메서드를 추가합니다.
    @Query("SELECT * FROM question WHERE id = :fromDate")
    suspend fun get(fromDate: String): QuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(vararg questions: QuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(questions: List<QuestionEntity>)

    @Query("SELECT * FROM question ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, QuestionEntity>

    @Query("DELETE FROM question")
    suspend fun deleteAll()
}