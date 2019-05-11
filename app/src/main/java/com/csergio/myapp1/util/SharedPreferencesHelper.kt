package com.csergio.myapp1.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

object SharedPreferencesHelper:AppCompatActivity() {

    val preferences = getSharedPreferences("UserCookie", Context.MODE_PRIVATE)

    fun getUserId():String{
        return preferences.getString("user_id", "")
    }

    fun getUserName():String{
        return preferences.getString("user_name", "")
    }

    fun getUserPic():String{
        return preferences.getString("user_pic", "")
    }

}