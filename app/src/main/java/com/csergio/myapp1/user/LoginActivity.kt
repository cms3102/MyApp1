
package com.csergio.myapp1.user

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.csergio.myapp1.MainActivity
import com.csergio.myapp1.R
import com.csergio.myapp1.model.User
import com.csergio.myapp1.util.PostService
import com.csergio.myapp1.util.RetrofitBuilder
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    lateinit var preferences:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        preferences = getSharedPreferences("UserCookie", Context.MODE_PRIVATE)

        if (preferences.getString("user_id", "") != ""){
            Toast.makeText(this, "자동 로그인되었습니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        loginActivity_signUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        lgoinActivity_loginButton.setOnClickListener {
            val user = User()
            user.user_id = loginActivity_editText_email.text.toString()
            user.user_pw = loginActivity_editText_pw.text.toString()

            RetrofitBuilder.retrofit.create(PostService::class.java)
                .requestLogin(user).enqueue(object : Callback<String>{
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful && response.body()?.substringBefore("/") == "true"){

                            val body = response.body()
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
}
