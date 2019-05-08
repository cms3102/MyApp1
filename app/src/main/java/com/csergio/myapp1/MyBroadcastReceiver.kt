package com.csergio.myapp1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.widget.Toast

class MyBroadcastReceiver:BroadcastReceiver() {

    private var myId = ""

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED){

            val preferences = context?.getSharedPreferences("UserCookie", Context.MODE_PRIVATE)
            myId = preferences?.getString("user_id", "").toString()

            if (myId.isNotEmpty()){
                if (!NotificationService.state){
                    Log.d("NotificationService", "MyBroadcastReceiver에서 NotificationService 실행함")
                    val serviceIntent = Intent(context, NotificationService::class.java)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        context?.startForegroundService(serviceIntent)
                    } else {
                        context?.startService(serviceIntent)
                    }

                } else {
                    Log.d("NotificationService", "MyBroadcastReceiver에서 NotificationService 실행 취소")
                }
            }

        } else {
            Toast.makeText(context, "서비스 시작 실패", Toast.LENGTH_SHORT).show()
        }

    }

}