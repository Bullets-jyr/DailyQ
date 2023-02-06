package kr.co.bullets.dailyq.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

// Entity는 데이터베이스의 테이블과 대응되는 클래스입니다. 클래스에 @Entity 어노테이션을 붙여 Entity를 정의하면,
// Entity로 지정된 클래스가 SQLite의 테이블이 되고, 멤버 변수들이 테이블의 컬럼이 됩니다.
// 그리고 Entity의 인스턴스는 테이블의 행(Row)이 됩니다.
// @Entity 태그가 붙은 클래스의 이름(UserEntity)이 자동으로 테이블의 이름이 되는데, 이를 변경하고 싶다면
// @Entity 태그의 tableName 속성으로 지정할 수 있습니다.
// @Entity(tableName = "user")
// 테이블의 기본키(Primary Key)는 @PrimaryKey 어노테이션으로 지정할 수 있습니다.
// SQLite에서는 정수형 기본키가 자동으로 증가하도록 설정할 수 있는데 @PrimaryKey의 autoGenerate 속성으로 사용 여부를 변경할 수 있습니다. 기본값은 false입니다.
// @PrimaryKey(autoGenerate = true)
// 테이블 컬럼의 이름은 클래스 필드의 이름을 사용합니다. @ColumnInfo 어노테이션의 name 속성으로 지정할 수 있고,
// 컬럼의 기본값도 마찬가지로 @ColumnInfo 어노테이션의 defaultValue 속성으로 지정할 수 있습니다.
// 주의할 것은 클래스 필드의 기본값과 테이블 컬럼의 기본값이 별개라는 것입니다.
// UserEntity에서 answerCount: Int = 0이라고 된 것은 클래스 필드의 기본값이므로 테이블에서의 기본값으로 사용되지 않습니다.
// 우측의 CREATE TABLE 명령에 기본값이 없는 것을 볼 수 있습니다.
// UserEntity에 어노테이션과 어노테이션 속성들을 사용했을 때 Room에서 생성한 CRATE TABLE 명령이 어떻게 달라지는지 비교해보세요.
// [코드 10-22]를 보고 Daily Q의 사용자 정보를 저장할 UserEntity를 추가합니다.
@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    var id: String,
    var name: String?,
    var description: String?,
    var photo: String?,
    var answerCount: Int,
    var followerCount: Int,
    var followingCount: Int,
    var isFollowing: Boolean,
    var updatedAt: Date?
)
