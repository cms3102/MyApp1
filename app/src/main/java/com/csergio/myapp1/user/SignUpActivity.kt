package com.csergio.myapp1.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.csergio.myapp1.R
import com.csergio.myapp1.model.User
import com.csergio.myapp1.util.PostService
import com.csergio.myapp1.util.RetrofitBuilder
import kotlinx.android.synthetic.main.activity_sign_up.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        signUpActivity_signUpButton.setOnClickListener {

            val pw1 = signUpActivity_editText_pw1.text.toString()
            val pw2 = signUpActivity_editText_pw2.text.toString()

            if (pw1 != pw2){
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newUser = User()
            newUser.user_id = signUpActivity_editText_email.text.toString()
            newUser.user_pw = pw1
            newUser.user_name = signUpActivity_editText_name.text.toString()

            val retrofitService = RetrofitBuilder.retrofit.create(PostService::class.java)
            retrofitService.requestSignUpPost(newUser).enqueue(object : Callback<String>{
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(this@SignUpActivity, "회원 가입 실패", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful){
                        Toast.makeText(this@SignUpActivity, "회원 가입 성공", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

            })

        }
    }

}
