package kr.co.bullets.dailyq.api

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

// 갤러리에서 사진을 선택하면 URI를 가져오는데,
// uploadImage() 메서드로 업로드할 수 있도록 URI에서 새로운 RequestBody를 만들어 반환하는 asRequestBody 확장 함수를 만듭니다.
fun Uri.asRequestBody(cr: ContentResolver): RequestBody {
    return object : RequestBody() {
        override fun contentType(): MediaType? = cr.getType(this@asRequestBody)?.toMediaTypeOrNull()

        override fun contentLength(): Long = -1

        override fun writeTo(sink: BufferedSink) {
            val source = cr.openInputStream(this@asRequestBody)?.source()
            source?.use { sink.writeAll(it) }
        }
    }
}