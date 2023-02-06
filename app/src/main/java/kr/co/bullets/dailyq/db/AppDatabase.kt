package kr.co.bullets.dailyq.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kr.co.bullets.dailyq.db.dao.QuestionDao
import kr.co.bullets.dailyq.db.dao.UserDao
import kr.co.bullets.dailyq.db.entity.QuestionEntity
import kr.co.bullets.dailyq.db.entity.UserEntity

// Room의 데이터베이스는 RoomDatabase를 확장한 클래스에 @Database 어노테이션을 붙여 지정할 수 있습니다.
// 여기서는 버전, Entity, DAO, 타입 컨버터, 마이그레이션 등 데이터베이스에 대한 설정을 할 수 있고,
// SQLite와의 연결을 관리합니다. [코드 10-25]는 앞에서 만든 DAO, Entity, TypeConverter를 사용하기 위해 설정합니다.
// @Database 어노테이션에서 entities 속성으로 사용할 Entity를 지정하고, version 속성으로 데이터베이스 스키마 버전을 지정합니다.
// Room의 DAO를 가져오기 위해서는 AppDatabase에 매개변수가 없고 DAO를 반환하는 추상 메서드가 있어야 합니다.
// 그러므로 UserDao를 반환하는 추상 메서드를 선언합니다.
// 데이터베이스 클래스에 @TypeConverters 어노테이션을 이용해 데이터 타입의 변환을 위해 만들었던 Converters 클래스를 등록합니다.
// 여기까지가 Room의 Database를 만들기 위한 필수 과정입니다. 이렇게 설정된 AppDatabase는 DAO와 마찬가지로 어노테이션 프로세서가
// Appdatabase_Impl.java를 생성합니다. AppDatabase 클래스를 Room.databaseBuilder로 넘겨 인스턴스를 만들어 사용하면 되는데,
// 이 과정이 다소 무거운 작업이고 앱을 작동하는 동안 Room은 계속 필요하기 때문에 싱글톤으로 만들어 사용하는 것을 권장합니다.
// 5장에서 ApiService를 싱글톤으로 만들었던 것 처럼 AppDatabase도 싱글톤으로 만들겠습니다.
// [코드 10-33]과 같이 AppDatabase에 QuestionEntity를 등록하고, QuestionDao를 반환하는 메서드를 선언합니다.
@Database(entities = [UserEntity::class, QuestionEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getUserDao(): UserDao
    abstract fun getQuestionDao(): QuestionDao

    // [코드 10-26]에서는 싱글톤을 만들 때 자주 사용되는 더블 체크 락킹(Double-checked locking) 패턴을 사용했습니다.
    // 이 방법은 5장에서 만든 싱글톤 관리 코드와 다르게 초기화 메서드가 필요 없고, getInstance() 메서드가 생성과 가져오는 역할을 모두 합니다.
    // getInstance() 메서드를 호출했을 때 이미 생성된 INSTANCE가 있을 경우 생성하지 않고 반환합니다.
    // INSTANCE가 비어있다면 syncronized로 동기화 블록을 만든 후 다시 INSTANCE가 비어있는지 확인하고 INSTANCE를 생성해 INSTANCE 변수에 보관합니다.
    // 이렇게 만들면 synchronized로 잠기는 영역을 최소화하면서 싱글톤을 사용할 수 있습니다.
    // 여기까지 논리적으로는 완벽하지만 기술적인 문제가 있습니다.
    // getInstance() 메서드가 여러 스레드에서 호출되는 경우 JVM의 성능을 최적화하기 위해 CPU 캐시를 사용하고
    // 이 과정에서 여러 인스턴스가 생성될 수 있습니다.
    // 이런 문제를 방지하기 위해 @Volatile 어노테이션을 붙여 항상 메인 메모리에서 INSTANCE 변수를 사용하도록 만듭니다.
    companion object {
        const val FILENAME = "dailyq.db"
        @Volatile var INSTANCE: AppDatabase? = null

        private fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                FILENAME
            // Room의 스키마가 변경되었기 때문에 QuestionDao로 QuestionDao로 QuestionEntity를 사용하려고 하면 다음의 오류가 발생합니다.
            // java.lang.IllegalStateException: Room cannot verify the data integrity.
            // Looks like you've changed schema but forgot to update the version number.
            // You can simply fix this by increasing the version number.
            // 스키마가 변경되면 마이그레이션 코드를 작성하거나 데이터베이스를 삭제하고 다시 시작해야 합니다.
            // 개발 과정에서는 스키마가 변경되는 일이 많으므로 데이터베이스를 삭제하고 다시 시작합니다.
            // [코드 10-34]처럼 Room 생성 시 fallbackToDestructiveMigration() 빌더 메서드를 호출하면 마이그레이션이 실패했을 때
            // 테이블을 모두 삭제하고 다시 테이블을 만듭니다.
            // 개발 과정에서 사용하기 편하지만 데이터도 모두 삭제되니 주의해서 사용해야 합니다.
            ).fallbackToDestructiveMigration()
                .build()
        }

//        fun getInstance(context: Context): AppDatabase = INSTANCE ?:
//        synchronized(this) {
//            INSTANCE ?: create(context).also {
//                INSTANCE = it
//            }
//        }

        fun init(context: Context) = INSTANCE ?: synchronized(this) {
            INSTANCE ?: create(context).also {
                INSTANCE = it
            }
        }

        fun getInstance(): AppDatabase = INSTANCE!!
    }
}