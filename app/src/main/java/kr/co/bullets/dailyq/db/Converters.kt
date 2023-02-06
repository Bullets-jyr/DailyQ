package kr.co.bullets.dailyq.db

import androidx.room.TypeConverter
import java.util.*

// UserEntity의 updatedAt 필드는 기본 자료형이 아닌 Date 타입을 사용했습니다.
// SQLite에서 지원하지 않는 타입을 저장하려면 타입을 변환해주는 타입 컨버터(Type Converter)를 제공해야 합니다.
// 타입 컨버터는 매개변수로 받은 데이터를 특정 타입으로 변환해서 반환하는 메서드에 @TypeConverter 어노테이션을 붙여 만들 수 있습니다.
// [코드 10-23]에서 Date 타입을 변환하기 위해 SQLite에서 지원하는 Long 타입으로 반환하는 toLong() 메서드를 정의했고,
// SQLite에 저장된 Long을 Date로 변환해서 가져오기 위해서 toDate() 메서드를 정의했습니다.
// 타입 컨버터는 RoomDatabase를 만들면서 사용하겠습니다.
class Converters {

    @TypeConverter
    fun toDate(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun toLong(value: Date?): Long? {
        return value?.time
    }
}