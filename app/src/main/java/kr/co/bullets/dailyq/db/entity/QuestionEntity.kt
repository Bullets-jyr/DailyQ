package kr.co.bullets.dailyq.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.*

// 타임라인의 질문들을 데이터베이스에 저장해 오프라인 상태에서도 볼 수 있도록 만들겠습니다.
// 먼저 질문을 Room에 저장하기 위해 사용할 [코드 10-30]의 QuestionEntity를 만듭니다.
// QuestionEntity는 API 응답에서 사용하는 [코드 5-2]의 Question과 거의 같습니다.
// 하나의 클래스를 두 곳에서 공유하도록 만들 수 있지만, 서로 역할이 다르기 때문에 별개의 클래스를 만들어 사용하는 것이 좋습니다.
@Entity(tableName = "question")
data class QuestionEntity(
    @PrimaryKey
    val id: LocalDate,
    val text: String,
    val answerCount: Int,
    val updateAt: Date,
    val createdAt: Date,
)