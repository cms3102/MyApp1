package com.csergio.myapp1.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.csergio.myapp1.R
import com.csergio.myapp1.model.User
import com.csergio.myapp1.util.PostService
import com.csergio.myapp1.util.RetrofitBuilder
import com.csergio.myapp1.util.SQLiteHelper
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.item_state_message.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsFragment:Fragment() {

    lateinit var preferences:SharedPreferences
    lateinit var myId:String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        preferences = activity!!.getSharedPreferences("UserCookie", Context.MODE_PRIVATE)
        myId = preferences.getString("user_id", "")

        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsFragment_button_stateMessage.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.item_state_message, null)
            val dialog = AlertDialog.Builder(context!!)
                .setView(view)
                .setNegativeButton("취소") { dialog, which ->
                    dialog.cancel()
                }
                .setPositiveButton("확인") { dialog, which ->

                    val user = User()
                    user.state_message = view.item_state_message_editText.text.toString()
                    Log.d("입력된 상태 메시지", "입력된 상태 메시지 : ${user.state_message}")
                    user.user_id = myId

                    RetrofitBuilder.retrofit.create(PostService::class.java)
                        .updateStateMessage(user).enqueue(object : Callback<String>{

                            override fun onFailure(call: Call<String>, t: Throwable) {
                                Toast.makeText(context, "상태 메시지 업데이트 실패", Toast.LENGTH_SHORT).show()
                            }

                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                if (response.isSuccessful){
                                    Log.d("상태 메시지 업데이트", "상태 메시지 업데이트 : ${response.body()}")
                                }
                            }

                        })
                }

            dialog.show()
        }
    }

}