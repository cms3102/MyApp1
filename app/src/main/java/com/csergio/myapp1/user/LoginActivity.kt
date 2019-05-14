
package com.csergio.myapp1.user

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.csergio.myapp1.MainActivity
import com.csergio.myapp1.R
import com.csergio.myapp1.chat.ChatRoomActivity
import com.csergio.myapp1.model.User
import com.csergio.myapp1.util.PostService
import com.csergio.myapp1.util.RetrofitBuilder
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var preferences:SharedPreferences
    private var myId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        requirePermissions()

        preferences = getSharedPreferences("UserCookie", Context.MODE_PRIVATE)
        myId = preferences.getString("user_id", "")
        // 저장된 아이디가 있으면 자동 로그인 처리
        if (myId != ""){
            if (intent.getStringExtra("pushMessage") != null){
                val chatRoomId = intent.getStringExtra("chatRoomId")
                val intent = Intent(this, ChatRoomActivity::class.java)
                intent.putExtra("chatRoomId", chatRoomId)
                startActivity(intent)
                Toast.makeText(this, "알람을 통해서 챗룸으로 이동", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "자동 로그인되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        loginActivity_signUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        lgoinActivity_loginButton.setOnClickListener {

            val camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            val externalWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            if (!camera && !externalWrite){
                Toast.makeText(this, "앱 사용에 필요한 권한을 허용해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = User()
            user.user_id = loginActivity_editText_email.text.toString()
            user.user_pw = loginActivity_editText_pw.text.toString()

            if (user.user_id.isNullOrEmpty() || user.user_pw.isNullOrEmpty()){
                Toast.makeText(this, "아이디 또는 비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 로그인 처리
            RetrofitBuilder.retrofit.create(PostService::class.java)
                .requestLogin(user).enqueue(object : Callback<String>{
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful && response.body()?.substringBefore("/") == "true"){

                            val body = response.body()
                            // 자동 로그인 처리를 위한 아이디 저장
                            preferences.edit()
                            .putString("user_id", user.user_id)
                            .putString("user_name", body?.substringAfter("/"))
                            .apply()

                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            Toast.makeText(this@LoginActivity, "로그인 되었습니다.", Toast.LENGTH_SHORT).show()
                            finish()

                        }
                    }

                })
        }
    }

    fun requirePermissions(){
        Log.d("권한 요청 메소드 실행", "권한 요청 메소드 실행")
        val permissionList = mutableListOf<String>()
        permissionList.add(Manifest.permission.CAMERA)
        permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val deniedPermissions = mutableListOf<String>()

        for (permission in permissionList){
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED){
                deniedPermissions.add(permission)
            }
        }

        if (deniedPermissions.isNotEmpty()){
            ActivityCompat.requestPermissions(this, deniedPermissions.toTypedArray(), 111)
        }

    }
}
