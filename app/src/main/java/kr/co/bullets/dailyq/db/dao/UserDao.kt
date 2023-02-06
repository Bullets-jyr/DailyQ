package kr.co.bullets.dailyq.db.dao

import androidx.room.*
import kr.co.bullets.dailyq.db.entity.UserEntity

// DAO는 Data Access Object의 두문자어로 데이터베이스에 접근해 데이터를 가져오거나 변경하는 역할을 합니다.
// 인터페이스나 추상 클래스에 @Dao 어노테이션을 붙여 정의하고,
// DAO의 메서드에 @Insert, @Update, @Delete, @Query 어노테이션을 붙여 데이터베이스에 접근해 Entity를 조작하는 메서드를 만들 수 있습니다.
// [코드 10-24]는 Daily Q의 UserEntity를 조작하기 위한 UserDao입니다.
// UserDao 인터페이스에 @Dao 어노테이션을 붙였습니다. 그리고 UserEntity를 데이터베이스에 쓰고, 갱신하고, 삭제하고, 읽어오는 추상 메서드를 선언하고
// 각각에 @Insert, @Update, @Delete, @Query 어노테이션을 붙였습니다.
// @Insert, @Update, @Delete 어노테이션은 Room이 자동으로 쿼리를 만들어주고,
// @Query 어노테이션은 직접 쿼리를 작성해야 합니다.
// DAO의 메서드의 구현은 Room 어노테이션 프로세서가 생성합니다.
// 생성된 파일은 [그림 10-5]와 같이 DailyQ\app\build\generated\source\kapt 이하의 같은 패키지에서 찾을 수 있습니다.
// 파일 이름은 원래의 파일 이름에 접미사로 _Impl이 붙습니다.
// 예를 들어 UserDao의 경우 UserDao_Impl.java 파일이 됩니다.
// SQLite에서 INSERT나 UPDATE 구문을 처리하는 중에 기본키 충돌이 발생할 수 있습니다.
// 예를 들어 User 테이블의 기본키는 아이디인데 이미 사용 중인 아이디로 가입을 하려는 경우에 충돌이 발생합니다.
// SQLite에서는 이런 충돌이 발생했을 때 어떻게 해결할 것인지 ON CONFLICT 구분으로 해결 방법을 선택할 수 있는데
// Room에서는 @Insert와 @Update 어노테이션의 onConflict 속성으로 지정할 수 있습니다.
// @Update(onConflict = OnConflictStrategy.REPLACE)
// suspend fun insert(vararg users: UserEntity)
// onConflict의 기본값은 OnConflictStrategy.ABORT이고 사용할 수 있는 값과 의미는 다음과 같습니다.
// * OnConflictStrategy.ABORT: 충돌이 발생하면 트랜잭션을 중단하고 되돌립니다.
// * OnConflictStrategy.REPLACE: 충돌이 발생하면 오래된 데이터를 새 데이터로 교체하고 트랜잭션을 계속합니다.
// * OnConflictStrategy.IGNORE: 충돌이 발생하면 새로운 데이터를 무시하고 트랜잭션을 계속합니다.
// @Query 어노테이션은 직접 쿼리문을 작성하면 쿼리 결과를 메서드의 반환 타입으로 만들어 돌려줍니다.
// 메서드의 매개변수는 콜론을 붙여 쿼리문에서 사용할 수 있습니다.
// @Query("SELECT * FROM user WHERE id = :uid")
// suspend fun get(uid: String): UserEntity?
// @Query는 다양한 반환 타입을 지원합니다. 사용자가 정의한 Entity나 Array, List, Cursor 등의 기본 타입뿐 아니라 Flow, LiveData, Observable도 지원하고
// Paging3 라이브러리와 함께 사용하면 PagingSource도 반환할 수 있습니다.
// @Query("SELECT * FROM user LIMIT 1")
// fun getEntity(): UserEntity
// @Query("SELECT * FROM user")
// fun getCursor(): UserEntity
// @Query("SELECT * FROM user")
// fun getList(): List<UserEntity>
// @Query("SELECT * FROM user")
// fun getLiveData(): LiveData<List<UserEntity>>
// @Query("SELECT * FROM user")
// fun getPagingSource(): PagingSource<Int, UserEntity>

@Dao
interface UserDao {

    @Insert
    suspend fun insert(vararg users: UserEntity)

    @Update
    suspend fun update(vararg users: UserEntity)

    @Delete
    suspend fun delete(vararg userEntity: UserEntity)

    @Query("SELECT * FROM user WHERE id = :uid")
    suspend fun get(uid: String): UserEntity?
}