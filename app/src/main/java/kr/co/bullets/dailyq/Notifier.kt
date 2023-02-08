package kr.co.bullets.dailyq

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

// 안드로이드 8(API 26)부터는 미리 알림 채널을 만들어야 하기 때문에 안드로이드 앱이 시작할 때 채널을 만듭니다.
// 이를 위해 답글과 팔로우를 위한 채널을 만드는 메서드를 각각 만들고, init() 메서드에서 호출하고 App.kt에서 호출해 채널을 만듭니다.
object Notifier {
    const val ANSWER_CHANNEL_ID = "answer"
    const val FOLLOW_CHANNEL_ID = "follow"
    val NOTI_ID = 1000

    fun init(context: Context) {
        createAnswerChannel(context)
        createFollwChannel(context)
    }

    fun createAnswerChannel(context: Context) {
        val nm = NotificationManagerCompat.from(context)
        val channel = NotificationChannel(ANSWER_CHANNEL_ID, context.getString(R.string.noti_answer_channel), NotificationManager.IMPORTANCE_DEFAULT)
        nm.createNotificationChannel(channel)
    }

    fun createFollwChannel(context: Context) {
        val nm = NotificationManagerCompat.from(context)
        val channel = NotificationChannel(FOLLOW_CHANNEL_ID, context.getString(R.string.noti_follow_channel), NotificationManager.IMPORTANCE_DEFAULT)
        nm.createNotificationChannel(channel)
    }

    fun showAnswerNotification(context: Context, username: String) {
        val contentText = context.getString(R.string.noti_answer_msg, username)
        val builder = NotificationCompat.Builder(context, ANSWER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dailyq)
            .setContentTitle(context.getString(R.string.noti_answer_channel))
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val nm = NotificationManagerCompat.from(context)
        nm.notify(contentText.hashCode(), builder.build())
    }

    fun showFollowNotification(context: Context, username: String) {
        val contentText = context.getString(R.string.noti_follow_msg, username)
        val builder = NotificationCompat.Builder(context, FOLLOW_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dailyq)
            .setContentTitle(context.getString(R.string.noti_follow_channel))
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val nm = NotificationManagerCompat.from(context)
        nm.notify(contentText.hashCode(), builder.build())
    }
}