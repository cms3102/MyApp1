package com.csergio.myapp1

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.github.nkzawa.socketio.client.IO

class NotificationService : Service() {

    private val io = IO.socket("http://192.168.0.13:3000")

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "알림 서비스 실행됨!", Toast.LENGTH_LONG).show()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}
