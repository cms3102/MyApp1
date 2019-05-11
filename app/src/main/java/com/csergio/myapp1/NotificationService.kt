package com.csergio.myapp1

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.csergio.myapp1.chat.ChatRoomActivity
import com.csergio.myapp1.fragments.ChatFragment
import com.csergio.myapp1.model.Message
import com.csergio.myapp1.user.LoginActivity
import com.csergio.myapp1.util.SQLiteHelper
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import java.lang.RuntimeException
import java.net.URISyntaxException

/**
 * 메시지 수신 및 알림을 위한 서비스 클래스
 * */
class NotificationService : Service() {

    private lateinit var preferences: SharedPreferences
    private var myId = ""
    private lateinit var sqliteHelper: SQLiteHelper

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Toast.makeText(this, "알림 서비스 실행됨!", Toast.LENGTH_LONG).show()

        Log.d("TAG", "NotificationService 실행됨")

        state = true

        preferences = getSharedPreferences("UserCookie", Context.MODE_PRIVATE)
        myId = preferences.getString("user_id", "")
        sqliteHelper = SQLiteHelper(applicationContext)

        Log.d("서비스 상태3", "서비스 상태3 : $state")

        // oreo 이후 foreground service 실행을 위한 기본 알림 생성
        createNotification("default", Message())

        try {

            io.on(Socket.EVENT_CONNECT) {
                Log.d("TAG", "NotificationService에서 소켓 접속 됨")
                io.emit("makeConnection", myId)
            }

            // 푸시 알림 처리 메소드
            io.on("receivePushMessage"){

                Log.d("receivePushMessage", "receivePushMessage 실행됨!!!!!!!!!!")

                val message = Message()
                message.content = it[0].toString()
                message.chatroom_id = it[1].toString()
                message.sender_id = it[2].toString()
                message.sender_name = it[3].toString()
                message.sender_pic = it[4].toString()

                Log.d("메시지 내용 확인", "${it[0]}, ${it[1]}, ${it[2]}, ${it[3]}, ${it[4]}")

                // 받은 메시지 DB 저장
                sqliteHelper.saveMessageToDB(message)

                // 대화방 내 메시지 목록 갱신
                if (ChatRoomActivity.state){
                    ChatRoomActivity.addLastMessage(message.chatroom_id)
                }

                // 대화방 목록 갱신
                if(ChatFragment.state){
                    ChatFragment.refreshChatRoomList()
                }

                // 내가 보낸 메세지일 경우 알림 방지
                if (message.sender_id != myId){
                    createNotification("message", message)
                }

            }

            io.connect()

        } catch (e: URISyntaxException) {
            e.printStackTrace()
            throw RuntimeException(e)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    companion object {

        private val io = IO.socket("http://192.168.0.13:3000")

        // 서비스 객체 중복 생성 방지를 위한 상태 변수
        var state = false

        fun getIO():Socket{
            return io
        }

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotification(type:String, message: Message){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notificationChannel = NotificationChannel("notification_message", "메시지 알림", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)

            val notificationBuilder = NotificationCompat.Builder(this@NotificationService, notificationChannel.id)
            when(type){
                "message" -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.putExtra("chatRoomId", message.chatroom_id)
                    Log.d("메시지 내용 확인3", "chatRoomId : ${message.chatroom_id}, myPicAddress : ${message.sender_pic}")
                    intent.putExtra("pushMessage", "pushMessage")
                    val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    notificationBuilder
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(message.sender_name)
                        .setContentText(message.content)
                        .setContentIntent(pendingIntent)
                }
                "default" -> {
                    notificationBuilder
                        .setSmallIcon(R.drawable.logo)
                }
            }

            startForeground(1, notificationBuilder.build())
//            notificationManager.notify(1, notificationBuilder.build())

        }

    }

    override fun onDestroy() {
        state = false
        super.onDestroy()
    }

}
