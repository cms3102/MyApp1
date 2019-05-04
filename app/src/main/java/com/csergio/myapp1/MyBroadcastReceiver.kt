package com.csergio.myapp1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class MyBroadcastReceiver:BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
//        Toast.makeText(context, "브로드캐스트 리시버 실행됨", Toast.LENGTH_LONG).show()
            Toast.makeText(context, "서비스 실행됨", Toast.LENGTH_LONG).show()
//            val serviceIntent = Intent(context, NotificationService::class.java)
//            context?.startService(serviceIntent)
    }

}