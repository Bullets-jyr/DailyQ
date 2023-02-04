package kr.co.bullets.dailyq.api.response

import java.time.LocalDate
import java.util.*

// User 클래스를 사용해 Answer 클래스에 answerer를 추가합니다.
// Answer를 응답으로 사용하는 API에
data class Answer(
    val qid: LocalDate,
    val uid: String,
    val text: String?,
    val photo: String?,
    val updatedAt: Date,
    val createdAt: Date,

    val answerer: User?
)
