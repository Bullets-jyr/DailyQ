package kr.co.bullets.dailyq.messaging

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kr.co.bullets.dailyq.Notifier

// FCM에서 전송한 메시지는 FirebaseMessagingService를 확장한 서비스로 전달됩니다.
// [코드 11-8]처럼 online.dailyq.messaging 패키지를 만들고 FirebaseMessagingService를 상속받는 MessagingService를 만듭니다.
// 그리고 FCM 백엔드에서 보낸 메시지를 수신하는 onMessageReceived에서 FCM의 두 가지 메시지 유형인 데이터 메시지와 알림 메시지를 가져올 수 있습니다.
// * 알림 메시지는 앱에서 다른 처리 없이 단순하게 서버에서 보내준 메시지를 알림으로 표시할 때 사용합니다.
// 앱이 백그라운드에 있을 땐 FCM SDK에서 자동으로 작업 표시줄에 표시하기 때문에 onMessageReceived() 메서드가 호출되지 않습니다.
// 앱이 포그라운드에 있을 땐 FCM SDK가 처리하지 않고 onMessageReceived() 메서드가 호출되며 RemoteMessage.getNotification() 메서드로 가져올 수 있습니다.
// * 데이터 메시지는 개발자의 필요에 따라 자유롭게 구성할 수 있기 때문에 알림 이외의 용도로도 사용할 수 있습니다.
// 친구 신청 알림을 보낼 때 보낸 사용자의 정보 메시지에 포함해 앱에서 저장할 수 있게 하거나
// 알림을 표시하지 않고 앱의 내부 작동에만 영향을 주는 트리거로 사용할 수 있습니다.
// 데이터 메시지가 앱에서 항상 직접 처리하고 유연하게 사용할 수 있기 때문에 Daily Q에 데이터 메시지를 사용합니다.
// [코드 11-8]에서는 onMessageReceived() 메서드에서 전달된 데이터 메시지를 이용해 알림을 표시하고 있습니다.
// remoteMessage.data는 Map<String, String> 타입으로, 개발하는 서비스의 API 문서를 보고 필요에 따라 사용하면 됩니다.
class MessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("onMessageReceived", "${remoteMessage.data}")

        remoteMessage.data.let {
            when (it["type"]) {
                "follow" -> {
                    Notifier.showFollowNotification(this, it["username"]!!)
                }
                "answer" -> {
                    Notifier.showAnswerNotification(this, it["username"]!!)
                }
            }
        }
    }
}