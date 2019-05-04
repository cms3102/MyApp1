package com.csergio.myapp1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast

class MyBroadcastReceiver:BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED){

            val serviceIntent = Intent(context, NotificationService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                context?.startForegroundService(serviceIntent)
            } else {
                context?.startService(serviceIntent)
            }

        } else {
            Toast.makeText(context, "서비스 시작 실패", Toast.LENGTH_SHORT).show()
        }

    }

}